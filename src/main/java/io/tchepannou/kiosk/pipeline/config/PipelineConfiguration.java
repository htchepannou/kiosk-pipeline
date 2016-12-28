package io.tchepannou.kiosk.pipeline.config;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsReader;
import io.tchepannou.kiosk.pipeline.consumer.ArticleMetadataConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ArticleValidationConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ContentExtractorConsumer;
import io.tchepannou.kiosk.pipeline.consumer.HtmlDownloadConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ImageExtractorConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ImageThumbnailConsumer;
import io.tchepannou.kiosk.pipeline.consumer.UrlExtractorConsumer;
import io.tchepannou.kiosk.pipeline.producer.FeedProducer;
import io.tchepannou.kiosk.pipeline.service.ThreadMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

@Configuration
public class PipelineConfiguration {
    private static final int CONSUMER_THREADS = 10;

    @Autowired
    AmazonSQS sqs;

    //-- Bean
    @Bean
    FeedProducer feedProducer() {
        return new FeedProducer();
    }

    @Bean
    ThreadMonitor threadMonitor() {
        return new ThreadMonitor();
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

    @Bean
    @Scope("prototype")
    ImageExtractorConsumer imageExtractorConsumer() {
        return new ImageExtractorConsumer();
    }

    @Bean
    @Scope("prototype")
    ImageThumbnailConsumer imageThumbnailConsumer() {
        return new ImageThumbnailConsumer();
    }

    @Bean
    @Scope("prototype")
    ArticleMetadataConsumer articleMetadataConsumer(){
        return new ArticleMetadataConsumer();
    }

    @Bean
    @Scope("prototype")
    ArticleValidationConsumer articleValidationConsumer(){
        return new ArticleValidationConsumer();
    }


    //-- Startup
    @PostConstruct
    public void init() {
        startUrlExtractor(CONSUMER_THREADS);
        startHtmlDownloader(CONSUMER_THREADS);
        startContentExtractor(CONSUMER_THREADS);

        startArticleMetadataConsumers(CONSUMER_THREADS);
        startArticleValidationConsumers(CONSUMER_THREADS);

        startImageExtractor(2 * CONSUMER_THREADS);
        startImageThumbnail(2 * CONSUMER_THREADS);

        feedProducer().produce();
    }

    //-- Thread
    private void startUrlExtractor(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final UrlExtractorConsumer consumer = urlExtractorConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor());
        }
    }

    private void startHtmlDownloader(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final HtmlDownloadConsumer consumer = htmlDownloadConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor());
        }
    }

    private void startContentExtractor(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final ContentExtractorConsumer consumer = contentExtractorConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor());
        }
    }

    private void startImageExtractor(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final ImageExtractorConsumer consumer = imageExtractorConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor());
        }
    }

    private void startImageThumbnail(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final ImageThumbnailConsumer consumer = imageThumbnailConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor());
        }
    }

    private void startArticleMetadataConsumers(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final ArticleMetadataConsumer consumer = articleMetadataConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor());
        }
    }

    private void startArticleValidationConsumers(final int threadCount){
        for (int i = 0; i < threadCount; i++) {
            final ArticleValidationConsumer consumer = articleValidationConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor());
        }
    }
}
