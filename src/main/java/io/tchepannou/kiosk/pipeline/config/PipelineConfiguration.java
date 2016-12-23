package io.tchepannou.kiosk.pipeline.config;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsReader;
import io.tchepannou.kiosk.pipeline.consumer.ContentExtractorConsumer;
import io.tchepannou.kiosk.pipeline.consumer.HtmlDownloadConsumer;
import io.tchepannou.kiosk.pipeline.consumer.UrlExtractorConsumer;
import io.tchepannou.kiosk.pipeline.processor.LoadFeedsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

@Configuration
public class PipelineConfiguration {
    public static final Logger LOGGER = LoggerFactory.getLogger(PipelineConfiguration.class);

    @Autowired
    AmazonSQS sqs;

    @Value("${kiosk.aws.sqs.feed}")
    String feedQueue;

    @Value("${kiosk.aws.sqs.url}")
    String urlQueue;

    @Value("${kiosk.aws.sqs.html}")
    String htmlQueue;

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
        startContentExtractor(10);
    }

    //-- Thread
    private void startUrlExtractor(final int threadCount) {
        final ThreadGroup group = new ThreadGroup(UrlExtractorConsumer.class.getSimpleName());
        for (int i = 0; i < threadCount; i++) {
            LOGGER.info("Starting Thread{} #{}", group.getName(), i);

            final SqsConsumer consumer = urlExtractorConsumer();
            final SqsReader reader = new SqsReader(feedQueue, sqs, consumer);
            final Thread thread = createThread(group, reader);
            thread.start();
        }
    }

    private void startHtmlDownloader(final int threadCount) {
        final ThreadGroup group = new ThreadGroup(HtmlDownloadConsumer.class.getSimpleName());
        for (int i = 0; i < threadCount; i++) {
            LOGGER.info("Starting Thread{} #{}", group.getName(), i);

            final SqsConsumer consumer = htmlDownloadConsumer();
            final SqsReader reader = new SqsReader(urlQueue, sqs, consumer);
            final Thread thread = createThread(group, reader);
            thread.start();
        }
    }

    private void startContentExtractor(final int threadCount) {
        final ThreadGroup group = new ThreadGroup(ContentExtractorConsumer.class.getSimpleName());
        for (int i = 0; i < threadCount; i++) {
            LOGGER.info("Starting Thread{} #{}", group.getName(), i);

            final SqsConsumer consumer = contentExtractorConsumer();
            final SqsReader reader = new SqsReader(htmlQueue, sqs, consumer);
            final Thread thread = createThread(group, reader);
            thread.start();
        }
    }

    private Thread createThread(final ThreadGroup group, final Runnable runnable) {
        final Thread thread = new Thread(group, runnable);
        thread.setDaemon(true);
        return thread;
    }
}
