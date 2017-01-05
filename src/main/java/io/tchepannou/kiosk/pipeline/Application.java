package io.tchepannou.kiosk.pipeline;

import io.tchepannou.kiosk.pipeline.producer.FeedProducer;
import io.tchepannou.kiosk.pipeline.producer.SimilarityMatrixProducer;
import io.tchepannou.kiosk.pipeline.service.ShutdownService;
import io.tchepannou.kiosk.pipeline.service.ThreadMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableSwagger2
public class Application {
    @Autowired
    FeedProducer feedProducer;

    @Autowired
    SimilarityMatrixProducer similarityMatrixProducer;

    public static void main(final String[] args) throws Exception {
        final ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);

        /* produce the feeds */
        ctx.getBean(FeedProducer.class).produce();

        /* consume the feeds URLs */
        ThreadMonitor monitor = ctx.getBean(ThreadMonitor.class);
        try {
            monitor.waitAllThreads(60000, 60000*30);

            /* generate the similarity matrix */
            ctx.getBean(SimilarityMatrixProducer.class).produce();

        } finally {
            ctx.getBean(ShutdownService.class).shutdown(0);
        }
    }
}
