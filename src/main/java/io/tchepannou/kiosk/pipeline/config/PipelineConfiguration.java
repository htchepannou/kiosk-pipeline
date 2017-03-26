package io.tchepannou.kiosk.pipeline.config;

import io.tchepannou.kiosk.core.service.Delay;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.core.service.MessageQueueProcessor;
import io.tchepannou.kiosk.core.service.impl.ConstantDelay;
import io.tchepannou.kiosk.pipeline.step.download.DownloadConsumer;
import io.tchepannou.kiosk.pipeline.step.url.FeedUrlProducer;
import io.tchepannou.kiosk.pipeline.step.url.UrlProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executor;

@Configuration
@ConfigurationProperties("kiosk.step")
public class PipelineConfiguration {
    @Autowired
    Executor executor;

    @Autowired
    @Qualifier("UrlMessageQueue")
    MessageQueue urlMessageQueue;

    @Autowired
    @Qualifier("MetadataMessageQueue")
    MessageQueue metadataMessageQueue;

    int workers;


    @PostConstruct
    public void run() {
        // Fetch URLs
        urlProducer().produce();

        // URL processing
        execute(downloadMessageQueueProcessor());
    }

    private void execute(Runnable runnable){
        for (int i=0 ; i<workers ; i++){
            executor.execute(runnable);
        }
    }

    //-- Common
    @Bean
    Delay delay(){
        return new ConstantDelay(60000);
    }

    //-- Url
    @Bean
    UrlProducer urlProducer(){
        return new UrlProducer();
    }

    @Bean
    FeedUrlProducer feedUrlProducer(){
        return new FeedUrlProducer();
    }

    //-- Download
    @Bean
    MessageQueueProcessor downloadMessageQueueProcessor (){
        return new MessageQueueProcessor(
                urlMessageQueue,
                downloadConsumer(),
                delay()
        );
    }

    @Bean
    @ConfigurationProperties("kiosk.step.DownloadConsumer")
    DownloadConsumer downloadConsumer(){
        return new DownloadConsumer();
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(final int workers) {
        this.workers = workers;
    }
}
