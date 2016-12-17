package io.tchepannou.kiosk.pipeline;

import io.tchepannou.kiosk.pipeline.service.ShutdownService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class Application {

    public static void main(final String[] args) throws Exception {
        final ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

        final ShutdownService service = ctx.getBean(ShutdownService.class);
        service.shutdown(0);
    }
}
