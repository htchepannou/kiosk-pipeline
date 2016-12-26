package io.tchepannou.kiosk.pipeline;

import io.tchepannou.kiosk.pipeline.service.ShutdownService;
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

        try {
            Thread.sleep(5 * 60 * 1000);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error", e);
        } finally {
            ctx.getBean(ShutdownService.class).shutdown(0);
        }
    }
}
