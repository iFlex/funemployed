package funemployed.game.metrics;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import funemployed.http.Endpoint;
import metrics_influxdb.HttpInfluxdbProtocol;
import metrics_influxdb.InfluxdbReporter;
import metrics_influxdb.api.measurements.CategoriesMetricMeasurementTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class InfluxdbMetricsSync {

    private static Logger logger = LoggerFactory.getLogger(Endpoint.class);

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

    List<ScheduledReporter> reporters = new LinkedList<>();

    public void addRegistry(MetricRegistry metrics) {
        try {
            ScheduledReporter reporter = InfluxdbReporter.forRegistry(metrics)
                    .protocol(new HttpInfluxdbProtocol("http", influxServer, influxServerPort, influxUser, influxPassword, influxDatabase))
                    .convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS)
                    .filter(MetricFilter.ALL)
                    .skipIdleMetrics(false)
                    .tag("application", "funemployed")
                    .transformer(new CategoriesMetricMeasurementTransformer("module", "artifact"))
                    .build();
            reporter.start(1, TimeUnit.SECONDS);
            reporters.add(reporter);
        } catch (Exception e){
            logger.error("Failed to initialise InfluxDB Metrics Reporter. Metrics will not be saved to the database",e);
        }
    }
}
