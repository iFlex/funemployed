package funemployed.http;

import funemployed.game.GameInstance;
import funemployed.game.GameInstanceFactory;
import funemployed.game.Player;
import funemployed.game.errors.GameException;
import funemployed.game.errors.PlayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class Endpoint {
    private static final String DEFAULT_LANGUAGE_PACK = "ro";
    private static Logger logger = LoggerFactory.getLogger(Endpoint.class);

    @Autowired
    private GameInstanceFactory gameInstanceFactory;

    @GetMapping("/greeting")
    public String greeting() {
        return "Hello World";
    }

    @GetMapping("/game-new")
    public GameInstance newGame(@RequestParam(value = "language", defaultValue = "ro") String language) {
        if(language == null){
            language = DEFAULT_LANGUAGE_PACK;
        }
        logger.info("*game-new:"+language);
        GameInstance gameInstance = gameInstanceFactory.newGame(language);
        if(gameInstance != null){
            return gameInstance;
        }
        return null;
    }

    @GetMapping("/{game_id}")
    public GameInstance getGameState(@PathVariable(value = "game_id") String gameId){
        return gameInstanceFactory.findGame(gameId);
    }

    @GetMapping("/{game_id}/player-add/{player_id}")
    public Player playerAdd(@PathVariable(value = "game_id") String gameId,
                            @PathVariable(value = "player_id") String playerId){
        logger.info("/player-add game:"+gameId+" player:"+playerId);

        GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
        if(gameInstance != null) {
            try {
                Player player = new Player(playerId);
                return gameInstance.addPlayer(player);
            } catch (Exception exc) {
                logger.error("/player-add ::" + exc.getMessage(),exc);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage(), exc);
            }
        }
        return null;
    }

    @GetMapping("/{game_id}/player-shuffle")
    public GameInstance playerShuffle(@PathVariable(value = "game_id") String gameId){
        logger.info("/player-shuffle game:"+gameId);

        GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
        if(gameInstance != null) {
            try {
                gameInstance.shufflePlayerOrder();
                return gameInstance;
            } catch (Exception exc) {
                logger.error("/player-shuffle",exc);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage(), exc);
            }
        }
        return null;
    }

    @GetMapping("/{game_id}/turn-start")
    public GameInstance turnStart(@PathVariable(value = "game_id") String gameId) {
        logger.info("/turn-start game:"+gameId);

        GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
        if(gameInstance != null){
            try {
                gameInstance.startTurn();
                return gameInstance;
            } catch (GameException exc) {
                logger.error("/turn-start",exc);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage(),exc);
            }
        }
        return null;
    }

    @GetMapping("/{game_id}/turn-start/force")
    public GameInstance turnStartForce(@PathVariable(value = "game_id") String gameId) {
        logger.info("/turn-start-force game:"+gameId);

        GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
        if(gameInstance != null) {
            gameInstance.forceNewTurn();
        }
        return gameInstance;
    }

    @GetMapping("/{game_id}/player-ready/{player_id}/{card_id_1}/{card_id_2}/{card_id_3}")
    public GameInstance playerReady(@PathVariable(value = "game_id") String gameId,
                              @PathVariable(value = "player_id") String playerId,
                              @PathVariable(value = "card_id_1") String card1,
                              @PathVariable(value = "card_id_2") String card2,
                              @PathVariable(value = "card_id_3") String card3) {
        logger.info("/player-ready game:"+gameId+" player:"+playerId+" cards:"+card1+","+card2+","+card3);

        GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
        if(gameInstance != null){
            try {
                Integer[] cards = new Integer[Player.REQUIRED_CANDIDATE_CARD_COUNT];
                cards[0] = Integer.valueOf(card1);
                cards[1] = Integer.valueOf(card2);
                cards[2] = Integer.valueOf(card3);

                gameInstance.playerReady(playerId,cards);
            } catch (GameException | PlayerException exc) {
                logger.error("/player-ready game:"+gameId+" player:"+playerId+" cards:"+card1+","+card2+","+card3+" ::" + exc.getMessage(),exc);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage(),exc);
            }
            return gameInstance;
        }
        return null;
    }

    @GetMapping("/{game_id}/player-unready/{player_id}")
    public GameInstance playerUnready(@PathVariable(value = "game_id") String gameId,
                                @PathVariable(value = "player_id") String playerId) throws GameException {
        logger.info("/player-unready game:"+gameId+" player:"+playerId);

        GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
        if(gameInstance != null) {
            try {
                gameInstance.playerUnready(playerId);
                return gameInstance;
            } catch (GameException exc) {
                logger.error("/player-unready",exc);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage(),exc);
            }
        }
        return null;
    }

    @GetMapping("/{game_id}/interview-start/{player_id}")
    public GameInstance interviewStart(@PathVariable(value = "game_id") String gameId,
                                       @PathVariable(value = "player_id") String playerId) {
        logger.info("/interview-start game:"+gameId+" player:"+playerId);

        GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
        if(gameInstance != null){
            try {
                gameInstance.startInterview(playerId);
                return gameInstance;
            } catch (GameException | PlayerException exc) {
                logger.error("/interview-start",exc);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage(),exc);
            }
        }
        return null;
    }

    @GetMapping("/{game_id}/interview-reveal/{player_id}/{card_id}")
    public GameInstance interviewReveal(@PathVariable(value = "game_id") String gameId,
                                        @PathVariable(value = "player_id") String playerId,
                                        @PathVariable(value = "card_id") String cardId) {
        logger.info("/interview-reveal game:"+gameId+" player:"+playerId+" card:"+cardId);

        GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
        if(gameInstance != null){
            try {
                gameInstance.revealCard(playerId, Integer.valueOf(cardId));
                return gameInstance;
            } catch (GameException | PlayerException exc) {
                logger.error("/interview-reveal",exc);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage(),exc);
            }
        }
        return null;
    }

    @GetMapping("/{game_id}/interview-end")
    public GameInstance interviewEnd(@PathVariable(value = "game_id") String gameId) {
        logger.info("/interview-end game:"+gameId+"winner:");

        GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
        if(gameInstance != null){
            try {
                gameInstance.endInterview();
                return gameInstance;
            } catch (GameException | PlayerException exc) {
                logger.error("/interview-end",exc);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage(),exc);
            }
        }
        return null;
    }

    @GetMapping("/{game_id}/turn-end/{hired_player_id}")
    public GameInstance turnEnd(@PathVariable(value = "game_id") String gameId,
                                @PathVariable(value = "hired_player_id") String playerId) {
        logger.info("/turn-end game:"+gameId+" winner:"+playerId);

        GameInstance gameInstance = gameInstanceFactory.findGame(gameId);
        if(gameInstance != null){
            try {
                gameInstance.endTurn(playerId);
                return gameInstance;
            } catch (GameException | PlayerException exc) {
                logger.error("/turn-end",exc);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exc.getMessage(),exc);
            }
        }
        return null;
    }
    //GET  /[game_id]/player-order                                 -> ["user_id_1", "user_id_2", "user_id_2"]
}
