package io.tchepannou.kiosk.pipeline.config;

import io.tchepannou.kiosk.core.service.Delay;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.core.service.MessageQueueProcessor;
import io.tchepannou.kiosk.core.service.impl.ConstantDelay;
import io.tchepannou.kiosk.pipeline.step.download.DownloadConsumer;
import io.tchepannou.kiosk.pipeline.step.metadata.MetadataConsumer;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilterSet;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleCountryFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleFeedFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleRegexFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleSuffixFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleVideoFilter;
import io.tchepannou.kiosk.pipeline.step.url.FeedUrlProducer;
import io.tchepannou.kiosk.pipeline.step.UrlProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Arrays;
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
        execute(metadataMessageQueueProcessor());
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

    //-- Metadata
    @Bean
    MessageQueueProcessor metadataMessageQueueProcessor (){
        return new MessageQueueProcessor(
                metadataMessageQueue,
                metadataConsumer(),
                delay()
        );
    }

    @Bean
    @ConfigurationProperties("kiosk.step.MetadataConsumer")
    MetadataConsumer metadataConsumer() {
        return new MetadataConsumer();
    }

    @Bean
    TitleFilter metadataTitleFilter (){
        return new TitleFilterSet(Arrays.asList(
                new TitleRegexFilter(),
                new TitleCountryFilter(),
                new TitleFeedFilter(),
                new TitleVideoFilter(),

                new TitleSuffixFilter() /* SHOULD BE THE LAST!!! */
        ));
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(final int workers) {
        this.workers = workers;
    }
}
