package funemployed.game.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vlkan.rfos.Clock;
import com.vlkan.rfos.RotatingFileOutputStream;
import com.vlkan.rfos.RotationConfig;
import com.vlkan.rfos.policy.SizeBasedRotationPolicy;
import com.vlkan.rfos.policy.TimeBasedRotationPolicy;
import funemployed.game.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.time.Instant;
import java.util.List;

@Service
public class GameStatisticsSync {
    private static Logger LOGGER = LoggerFactory.getLogger(GameStatisticsSync.class);
    private RotatingFileOutputStream stream;
    private ObjectMapper om = new ObjectMapper();

    public GameStatisticsSync(@Value("${gamestats_path:}") String basePath) {
        String baseFile = basePath + "/gamestats.log";
        TimeBasedRotationPolicy timeBasedRotationPolicy = new TimeBasedRotationPolicy() {
            @Override
            public Instant getTriggerInstant(Clock clock) {
                return clock.now().plusSeconds(60);
            }

            @Override
            protected Logger getLogger() {
                return LOGGER;
            }
        };

        RotationConfig config = RotationConfig
                .builder()
                .file(baseFile)
                .filePattern(basePath+"/gamestats-%d{yyyyMMdd-HHmmss.SSS}.log")
                .policy(new SizeBasedRotationPolicy(1024 * 1024 * 10 /* 10MiB */))
                .policy(timeBasedRotationPolicy)
                .build();

        stream = new RotatingFileOutputStream(config);
        timeBasedRotationPolicy.start(stream);
    }

    public synchronized void reportCardsWon(String gameId, Card job, List<Card> winners) {
        StringWriter stringWriter = new StringWriter();
        stringWriter.write(gameId);
        stringWriter.write(",");
        try {
            om.writeValue(stringWriter, job);
            for (Card card : winners) {
                stringWriter.write(",");
                om.writeValue(stringWriter, card);
            }
            stringWriter.write("\n");
            stringWriter.close();
        } catch (Exception e){
            LOGGER.error("Failed to marshall card stats", e);
            return;
        }

        try {
            stream.write(stringWriter.toString().getBytes());
        } catch (Exception e){
            LOGGER.error("Failed to write card stats",e);
        }
    }

    public synchronized void reportCardsContended(Card job, List<Card> contenders) {

    }
}
