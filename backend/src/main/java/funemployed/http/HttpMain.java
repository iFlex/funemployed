package funemployed.http;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(basePackages = "funemployed")
public class HttpMain {
    public static void main(String[] args) {
        System.setProperty("server.connection-timeout","60000");
        SpringApplication.run(HttpMain.class, args);
    }
}
