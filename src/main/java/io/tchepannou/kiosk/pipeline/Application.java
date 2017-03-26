package io.tchepannou.kiosk.pipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class Application {
    public static void main(final String[] args) throws Exception {
        final ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

        /* consume the feeds URLs */
//        try {
//            ctx.getBean(AwsPipelineRunner.class).run();
//        } finally {
//            ctx.getBean(ShutdownService.class).shutdown(0);
//        }
    }
}
