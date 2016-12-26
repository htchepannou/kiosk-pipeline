package io.tchepannou.kiosk.pipeline.config;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsReader;
import io.tchepannou.kiosk.pipeline.consumer.ContentExtractorConsumer;
import io.tchepannou.kiosk.pipeline.consumer.HtmlDownloadConsumer;
import io.tchepannou.kiosk.pipeline.consumer.UrlExtractorConsumer;
import io.tchepannou.kiosk.pipeline.processor.LoadFeedsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

@Configuration
public class PipelineConfiguration {
    public static final Logger LOGGER = LoggerFactory.getLogger(PipelineConfiguration.class);

    @Autowired
    AmazonSQS sqs;

    //-- Bean
    @Bean
    LoadFeedsProcessor loadFeedsProcessor() {
        return new LoadFeedsProcessor();
    }

    @Bean
    @Scope("prototype")
    UrlExtractorConsumer urlExtractorConsumer() {
        return new UrlExtractorConsumer();
    }

    @Bean
    @Scope("prototype")
    HtmlDownloadConsumer htmlDownloadConsumer() {
        return new HtmlDownloadConsumer();
    }

    @Bean
    @Scope("prototype")
    ContentExtractorConsumer contentExtractorConsumer() {
        return new ContentExtractorConsumer();
    }



    //-- Startup
    @PostConstruct
    public void init() {
        startUrlExtractor(5);
        startHtmlDownloader(10);
//        startContentExtractor(10);
    }

    //-- Thread
    private void startUrlExtractor(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final UrlExtractorConsumer consumer = urlExtractorConsumer();
            final SqsReader reader = new SqsReader(consumer.getInputQueue(), sqs, consumer);
            reader.start();
        }
    }

    private void startHtmlDownloader(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final HtmlDownloadConsumer consumer = htmlDownloadConsumer();
            final SqsReader reader = new SqsReader(consumer.getInputQueue(), sqs, consumer);
            reader.start();
        }
    }

    private void startContentExtractor(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final ContentExtractorConsumer consumer = contentExtractorConsumer();
            final SqsReader reader = new SqsReader(consumer.getInputQueue(), sqs, consumer);
            reader.start();
        }
    }
}
