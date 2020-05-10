package funemployed.game.expirer;

import funemployed.game.GameInstance;
import funemployed.game.Player;
import funemployed.game.errors.GameException;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Date;
import java.util.concurrent.Executor;

//@Service
public class TimeBasedExpirer implements Runnable{
    public static final Logger LOGGER = LoggerFactory.getLogger(TimeBasedExpirer.class);
    public static final int MAX_INACTIVITY = 30000;
    Executor executor= new ConcurrentTaskExecutor();
    private Map<Pair<Player,GameInstance>, Date> lastSeen;
    private Thread t;

    @PostConstruct
    public void start(){
        t = new Thread(this);
        t.start();
    }

    //ToDo: check if this leaks futures by leaving them in a queue for them to pile up if the program never checks the result
    public synchronized void update(Player p, GameInstance gi) {
        executor.execute(() -> {
            synchronized (lastSeen) {
                Pair<Player, GameInstance> pair = new Pair<>(p, gi);
                lastSeen.put(pair, Date.from(Instant.now()));
            }
        });
    }


    public void expire(Player p, GameInstance gi) {
        executor.execute(() -> {
            try {
                gi.removePlayer(p.getId());
                LOGGER.error("Player {} has TIMED_OUT from game {} and has been removed.", p.getId(), gi.getId());
            } catch (GameException e){
                LOGGER.error("Failed to expire player {} from game {}", p.getId(), gi.getId(), e);
            }
        });
    }

    public void run(){
        //iterate and pick out timeouts
        //each timeout removes the player from the game
        //is the game has no more players -> unpersist
        while(true){
            synchronized (lastSeen){
                Iterator<Map.Entry<Pair<Player,GameInstance>, Date>> iterator = lastSeen.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<Pair<Player,GameInstance>, Date> next = iterator.next();
                    Player p = next.getKey().getKey();
                    GameInstance gi = next.getKey().getValue();
                    Date lastSeenDate = next.getValue();

                    long delta = Date.from(Instant.now()).getTime() - lastSeenDate.getTime();
                    if( delta >= MAX_INACTIVITY ) {
                        expire(p,gi);
                    }
                }
            }

            try {
                Thread.sleep(1000);
            } catch(Exception e){

            }
        }
    }
}
