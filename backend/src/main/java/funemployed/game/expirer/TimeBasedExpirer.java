package funemployed.game.expirer;

import funemployed.game.GameInstance;
import funemployed.game.Player;
import javafx.util.Pair;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Date;

@Service
public class TimeBasedExpirer {

    public static final int MAX_INACTIVITY = 30000;
    private Map<Pair<String,String>, Date> lastSeen;

//    public synchronized void update(Player p, GameInstance gi) {
//        synchronized (lastSeen) {
//            Pair<String, String> pair = new Pair<>(gi.getId(), p.getId());
//            lastSeen.put(pair, Date.from(Instant.now()));
//        }
//    }
//
//    public void expire(Pair<String, String> expired) {
//
//    }
//
//    public void run(){
//        //iterate and pick out timeouts
//        //each timeout removes the player from the game
//        //is the game has no more players -> unpersist
//        while(true){
//            synchronized (lastSeen){
//                for() {
//                    Pair<String,String> pair = null;
//                    Date lastSeenDate = null;
//
//                    long delta = Date.from(Instant.now()).getTime() - lastSeenDate.getTime();
//                    if( delta >= MAX_INACTIVITY ) {
//                        expire(pair);
//                    }
//                }
//            }
//
//            try {
//                Thread.sleep(1000);
//            } catch(Exception e){
//
//            }
//        }
//    }
}
