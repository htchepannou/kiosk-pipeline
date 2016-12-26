package io.tchepannou.kiosk.pipeline;

import io.tchepannou.kiosk.pipeline.service.ShutdownService;
import io.tchepannou.kiosk.pipeline.service.ThreadMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(final String[] args) throws Exception {
        final ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

        ThreadMonitor monitor = ctx.getBean(ThreadMonitor.class);
        try {
            monitor.waitAllThreads(60000, 60000*30);
        } finally {
            ctx.getBean(ShutdownService.class).shutdown(0);
        }
    }
}
