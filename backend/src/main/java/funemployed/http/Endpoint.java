package funemployed.http;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.*;
import funemployed.game.GameInstance;
import funemployed.game.GameInstanceFactory;
import funemployed.game.Player;
import funemployed.game.errors.DeckException;
import funemployed.game.errors.GameException;
import funemployed.game.errors.PlayerException;
import funemployed.game.persisters.PersisterService;
import metrics_influxdb.HttpInfluxdbProtocol;
import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.api.measurements.CategoriesMetricMeasurementTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import static com.codahale.metrics.MetricRegistry.name;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class Endpoint {
    private static final String DEFAULT_LANGUAGE_PACK = "ro";
    private static final String TOKEN_COOKIE_KEY = "token";
    private static final String USER_COOKIE_KEY = "token";
    private static final int TOKEN_LENGTH = 64;
    private static Logger logger = LoggerFactory.getLogger(Endpoint.class);
    private Map<String, String> userToToken = new HashMap<>();

    //metrics
    private final MetricRegistry metrics = new MetricRegistry();
    private final Meter requests = metrics.meter("requests");
    private final Timer gameNewTimer = metrics.timer(name(Endpoint.class, "game-new"));
    private final Timer playerAddTimer = metrics.timer(name(Endpoint.class, "player-add"));
    private final Timer playerRemoveTimer = metrics.timer(name(Endpoint.class, "player-remove"));
    private final Timer playerShuffleTimer = metrics.timer(name(Endpoint.class, "player-shuffle"));
    private final Timer turnStartTimer = metrics.timer(name(Endpoint.class, "turn-start"));
    private final Timer playerReadyTimer = metrics.timer(name(Endpoint.class, "player-ready"));
    private final Timer playerUnreadyTimer = metrics.timer(name(Endpoint.class, "player-unready"));
    private final Timer interviewStartTimer = metrics.timer(name(Endpoint.class, "interview-start"));
    private final Timer interviewRevealTimer = metrics.timer(name(Endpoint.class, "interview-reveal"));
    private final Timer interviewEndTimer = metrics.timer(name(Endpoint.class, "interview-end"));
    private final Timer turnEndTimer = metrics.timer(name(Endpoint.class, "turn-end"));
    private final Timer gameStatusTimer = metrics.timer(name(Endpoint.class, "game-status"));

    @Value("${influx.server:localhost}")
    String influxServer;
    @Value("${influx.port:8086}")
    int influxServerPort;
    @Value("${influx.user:root}")
    String influxUser;
    @Value("${influx.password:root}")
    String influxPassword;
    @Value("${influx.db:funemployed}")
    String influxDatabase;

    ScheduledReporter reporter;

    @Value("${ignore_cookies:false}")
    private Boolean ignoreCookies;

    @Autowired
    private GameInstanceFactory gameInstanceFactory;

    @Autowired
    PersisterService persisterService;

    @Autowired
    public void init() {
        try {
            reporter = InfluxdbReporter.forRegistry(metrics)
                    .protocol(new HttpInfluxdbProtocol("http", influxServer, influxServerPort, influxUser, influxPassword, influxDatabase))
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .filter(MetricFilter.ALL)
                    .skipIdleMetrics(false)
                    .tag("application", "funemployed")
                    //.tag("client", "OurImportantClient")
                    //.tag("server", serverIP)
                    .transformer(new CategoriesMetricMeasurementTransformer("module", "artifact"))
                    .build();
            reporter.start(1, TimeUnit.SECONDS);
        } catch (Exception e){
            logger.error("Failed to initialise InfluxDB Metrics Reporter. Metrics will not be saved to the database",e);
        }
    }

    private synchronized String newToken(String userId){
        if(ignoreCookies){
            return "";
        }

        String token = "blablah";
        userToToken.put(userId, token);
        return token;
    }

    private synchronized boolean authorize(String userId, String token){
        if(ignoreCookies){
            return true;
        }

        String correctToken = userToToken.get(userId);
        return token != null && token.length() == TOKEN_LENGTH && correctToken == token;
    }

    private synchronized boolean unauthorize(String userId, String token){
        if(ignoreCookies){
            return true;
        }

        if(authorize(userId, token)) {
            userToToken.remove(userId);
            return true;
        }
        return false;
    }

    //ToDo: rate limit
    @GetMapping("/game-new")
    public GameInstance newGame(@RequestParam(value = "language", defaultValue = "ro") String language,
                                @CookieValue(value = TOKEN_COOKIE_KEY, defaultValue = "") String token,
                                @CookieValue(value = USER_COOKIE_KEY, defaultValue = "") String cookieUserId) {
        if(language == null){
            language = DEFAULT_LANGUAGE_PACK;
        }
        logger.info("*game-new:"+language);

        requests.mark();
        try(final Timer.Context context = gameNewTimer.time()) {
            GameInstance gameInstance = gameInstanceFactory.newGame(language);
            if (gameInstance != null) {
                persisterService.update(gameInstance);
                return gameInstance;
            }
            return null;
        }
    }

    //ToDO: rate limit
    @GetMapping("/{game_id}")
    public GameInstance getGameState(@PathVariable(value = "game_id") String gameId){
        requests.mark();
        try(final Timer.Context context = gameStatusTimer.time()) {
            return gameInstanceFactory.findGame(gameId);
        }
    }

    //ToDo: figure out
    @GetMapping("/{game_id}/player-add/{player_id}")
    public Player playerAdd(@PathVariable(value = "game_id") String gameId,
                            @PathVariable(value = "player_id") String playerId,
                            @CookieValue(value = TOKEN_COOKIE_KEY, defaultValue = "") String token,
                            @CookieValue(value = USER_COOKIE_KEY, defaultValue = "") String cookieUserId,
                            HttpServletResponse response){
        logger.info("/player-add game:"+gameId+" player:"+playerId);

        requests.mark();
        try(final Timer.Context context = playerAddTimer.time()) {
            GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
            if (gameInstance != null) {
                Player player = new Player(playerId);
                player = gameInstance.addPlayer(player);

                persisterService.update(gameInstance);

                Cookie tokenCookie = new Cookie(TOKEN_COOKIE_KEY, token);
                Cookie userIdCookie = new Cookie(USER_COOKIE_KEY, player.getId());
                response.addCookie(tokenCookie);
                response.addCookie(userIdCookie);

                return player;
            }
            return null;
        }
    }

    @GetMapping("/{game_id}/player-remove/{player_id}")
    public Player playerRemove(@PathVariable(value = "game_id") String gameId,
                               @PathVariable(value = "player_id") String playerId,
                               @CookieValue(value = TOKEN_COOKIE_KEY, defaultValue = "") String token,
                               @CookieValue(value = USER_COOKIE_KEY, defaultValue = "") String cookieUserId)
                                throws GameException {
        logger.info("/player-remove game:"+gameId+" player:"+playerId);

        requests.mark();
        try(final Timer.Context context = playerRemoveTimer.time()) {
            if (!unauthorize(playerId, token)) {
                throw new GameException("Unauthorised");
            }

            GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
            if (gameInstance != null) {
                Player p = gameInstance.removePlayer(playerId);
                if (gameInstance.getPlayers().size() == 0) {
                    gameInstanceFactory.endGame(gameInstance.getId());
                    persisterService.delete(gameInstance.getId());
                } else {
                    persisterService.update(gameInstance);
                }
                return p;
            }
            return null;
        }
    }

    @GetMapping("/{game_id}/player-shuffle")
    public GameInstance playerShuffle(@PathVariable(value = "game_id") String gameId,
                                      @CookieValue(value = TOKEN_COOKIE_KEY, defaultValue = "") String token,
                                      @CookieValue(value = USER_COOKIE_KEY, defaultValue = "") String cookieUserId){
        logger.info("/player-shuffle game:"+gameId);

        requests.mark();
        try(final Timer.Context context = playerShuffleTimer.time()) {
            GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
            if (gameInstance != null) {
                gameInstance.shufflePlayerOrder();
                persisterService.update(gameInstance);
            }
            return gameInstance;
        }
    }

    @GetMapping("/{game_id}/turn-start")
    public GameInstance turnStart(@PathVariable(value = "game_id") String gameId,
                                  @CookieValue(value = TOKEN_COOKIE_KEY, defaultValue = "") String token,
                                  @CookieValue(value = USER_COOKIE_KEY, defaultValue = "") String cookieUserId)
                                    throws GameException {
        logger.info("/turn-start game:"+gameId);

        requests.mark();
        try(final Timer.Context context = turnStartTimer.time()) {
            GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
            if (gameInstance != null) {
                gameInstance.startTurn();
                persisterService.update(gameInstance);
            }
            return gameInstance;
        }
    }

    @GetMapping("/{game_id}/turn-start/force")
    public GameInstance turnStartForce(@PathVariable(value = "game_id") String gameId,
                                       @CookieValue(value = TOKEN_COOKIE_KEY, defaultValue = "") String token,
                                       @CookieValue(value = USER_COOKIE_KEY, defaultValue = "") String cookieUserId) {
        logger.info("/turn-start-force game:"+gameId);

        requests.mark();
        try(final Timer.Context context = turnStartTimer.time()) {
            GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
            if (gameInstance != null) {
                gameInstance.forceNewTurn();
                persisterService.update(gameInstance);
            }
            return gameInstance;
        }
    }

    @GetMapping("/{game_id}/player-ready/{player_id}/{card_id_1}/{card_id_2}/{card_id_3}")
    public GameInstance playerReady(@PathVariable(value = "game_id") String gameId,
                              @PathVariable(value = "player_id") String playerId,
                              @PathVariable(value = "card_id_1") String card1,
                              @PathVariable(value = "card_id_2") String card2,
                              @PathVariable(value = "card_id_3") String card3,
                              @CookieValue(value = TOKEN_COOKIE_KEY, defaultValue = "") String token,
                              @CookieValue(value = USER_COOKIE_KEY, defaultValue = "") String cookieUserId)
                                throws GameException, PlayerException {
        logger.info("/player-ready game:"+gameId+" player:"+playerId+" cards:"+card1+","+card2+","+card3);

        requests.mark();
        try(final Timer.Context context = playerReadyTimer.time()) {
            if (!authorize(playerId, token)) {
                throw new GameException("Unauthorised");
            }

            GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
            if (gameInstance != null) {
                Integer[] cards = new Integer[Player.REQUIRED_CANDIDATE_CARD_COUNT];
                cards[0] = Integer.valueOf(card1);
                cards[1] = Integer.valueOf(card2);
                cards[2] = Integer.valueOf(card3);

                gameInstance.playerReady(playerId, cards);
                persisterService.update(gameInstance);
            }
            return gameInstance;
        }
    }

    @GetMapping("/{game_id}/player-unready/{player_id}")
    public GameInstance playerUnready(@PathVariable(value = "game_id") String gameId,
                                @PathVariable(value = "player_id") String playerId,
                                @CookieValue(value = TOKEN_COOKIE_KEY, defaultValue = "") String token,
                                @CookieValue(value = USER_COOKIE_KEY, defaultValue = "") String cookieUserId)
                                    throws GameException {
        logger.info("/player-unready game:"+gameId+" player:"+playerId);

        requests.mark();
        try(final Timer.Context context = playerUnreadyTimer.time()) {
            if (!authorize(playerId, token)) {
                throw new GameException("Unauthorised");
            }

            GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
            if (gameInstance != null) {
                gameInstance.playerUnready(playerId);
                persisterService.update(gameInstance);
            }
            return gameInstance;
        }
    }

    @GetMapping("/{game_id}/interview-start/{player_id}")
    public GameInstance interviewStart(@PathVariable(value = "game_id") String gameId,
                                       @PathVariable(value = "player_id") String playerId,
                                       @CookieValue(value = TOKEN_COOKIE_KEY, defaultValue = "") String token,
                                       @CookieValue(value = USER_COOKIE_KEY, defaultValue = "") String cookieUserId)
                                        throws GameException, PlayerException {
        logger.info("/interview-start game:"+gameId+" player:"+playerId);

        requests.mark();
        try(final Timer.Context context = interviewStartTimer.time()) {
            if (!authorize(playerId, token)) {
                throw new GameException("Unauthorised");
            }

            GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
            if (gameInstance != null) {
                gameInstance.startInterview(playerId);
                persisterService.update(gameInstance);
            }
            return gameInstance;
        }
    }

    @GetMapping("/{game_id}/interview-reveal/{player_id}/{card_id}")
    public GameInstance interviewReveal(@PathVariable(value = "game_id") String gameId,
                                        @PathVariable(value = "player_id") String playerId,
                                        @PathVariable(value = "card_id") String cardId,
                                        @CookieValue(value = TOKEN_COOKIE_KEY, defaultValue = "") String token,
                                        @CookieValue(value = USER_COOKIE_KEY, defaultValue = "") String cookieUserId)
                                            throws GameException, PlayerException {
        logger.info("/interview-reveal game:"+gameId+" player:"+playerId+" card:"+cardId);

        requests.mark();
        try(final Timer.Context context = interviewRevealTimer.time()) {
            if (!authorize(playerId, token)) {
                throw new GameException("Unauthorised");
            }
            GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
            if (gameInstance != null) {
                gameInstance.revealCard(playerId, Integer.valueOf(cardId));
                persisterService.update(gameInstance);
            }
            return gameInstance;
        }
    }

    @GetMapping("/{game_id}/interview-end")
    public GameInstance interviewEnd(@PathVariable(value = "game_id") String gameId,
                                     @CookieValue(value = TOKEN_COOKIE_KEY, defaultValue = "") String token,
                                     @CookieValue(value = USER_COOKIE_KEY, defaultValue = "") String cookieUserId)
                                        throws GameException, PlayerException {
        logger.info("/interview-end game:"+gameId+"winner:");

        requests.mark();
        try(final Timer.Context context = interviewEndTimer.time()) {

            //        if(!authorize(playerId, token)){
//            throw new GameException("Unauthorised");
//        }

            GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
            if (gameInstance != null) {
                gameInstance.endInterview();
                persisterService.update(gameInstance);
            }
            return gameInstance;
        }
    }

    @GetMapping("/{game_id}/turn-end/{hired_player_id}")
    public GameInstance turnEnd(@PathVariable(value = "game_id") String gameId,
                                @PathVariable(value = "hired_player_id") String playerId,
                                @CookieValue(value = TOKEN_COOKIE_KEY, defaultValue = "") String token,
                                @CookieValue(value = USER_COOKIE_KEY, defaultValue = "") String cookieUserId)
                                    throws GameException, PlayerException {
        logger.info("/turn-end game:"+gameId+" winner:"+playerId);

        requests.mark();
        try(final Timer.Context context = turnEndTimer.time()) {

//        if(!authorize(, token)){
//            throw new GameException("Unauthorised");
//        }

            GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
            if (gameInstance != null) {
                gameInstance.endTurn(playerId);
                persisterService.update(gameInstance);
            }
            return gameInstance;
        }
    }
    //GET  /[game_id]/player-order                                 -> ["user_id_1", "user_id_2", "user_id_2"]

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleExceptions(Exception ex) {
        logger.error("Encountered exception", ex);

        if(ex instanceof GameException || ex instanceof PlayerException || ex instanceof DeckException){
            return new ResponseEntity<>(
                    new ApiError(ex.getClass().getSimpleName(), ex.getMessage()),
                    HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(
                new ApiError("invalid_operation","cold not perform your operation"),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
