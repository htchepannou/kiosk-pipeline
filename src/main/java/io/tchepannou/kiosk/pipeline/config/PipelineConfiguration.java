package io.tchepannou.kiosk.pipeline.config;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsReader;
import io.tchepannou.kiosk.pipeline.consumer.ArticleDedupConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ArticleMetadataConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ArticleValidationConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ArticleContentExtractorConsumer;
import io.tchepannou.kiosk.pipeline.consumer.HtmlDownloadConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ImageExtractorConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ImageMainConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ImageThumbnailConsumer;
import io.tchepannou.kiosk.pipeline.consumer.UrlExtractorConsumer;
import io.tchepannou.kiosk.pipeline.consumer.VideoExtractorConsumer;
import io.tchepannou.kiosk.pipeline.producer.PublishProducer;
import io.tchepannou.kiosk.pipeline.producer.FeedProducer;
import io.tchepannou.kiosk.pipeline.producer.SimilarityMatrixProducer;
import io.tchepannou.kiosk.pipeline.service.PipelineRunner;
import io.tchepannou.kiosk.pipeline.service.ThreadMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

@Configuration
public class PipelineConfiguration {
    private static final int CONSUMER_THREADS = 10;

    @Autowired
    AmazonSQS sqs;

    @Value("${kiosk.pipeline.maxThreadWait}")
    int maxThreadWait;

    @Value("${kiosk.pipeline.autoStartPipeline}")
    boolean autoStartPipeline;

    //-- Beans
    @Bean
    ThreadMonitor threadMonitor() {
        return new ThreadMonitor();
    }

    @Bean
    PipelineRunner pipelineRunner(){
        return new PipelineRunner();
    }


    //-- Producer
    @Bean
    FeedProducer feedProducer() {
        return new FeedProducer();
    }

    @Bean
    @ConfigurationProperties("kiosk.pipeline.SimilarityMatrixProducer")
    SimilarityMatrixProducer similarityMatrixProducer() {
        return new SimilarityMatrixProducer();
    }

    @Bean
    PublishProducer articlePublishProducer() {
        return new PublishProducer();
    }


    //-- Consumers
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
    ImageMainConsumer imageMainConsumer() {
        return new ImageMainConsumer();
    }

    @Bean
    @Scope("prototype")
    VideoExtractorConsumer videoExtractorConsumer() {
        return new VideoExtractorConsumer();
    }

    @Bean
    @Scope("prototype")
    @ConfigurationProperties("kiosk.pipeline.ArticleContentExtractorConsumer")
    ArticleContentExtractorConsumer articleContentExtractorConsumer() {
        return new ArticleContentExtractorConsumer();
    }

    @Bean
    @Scope("prototype")
    @ConfigurationProperties("kiosk.pipeline.ArticleMetadataConsumer")
    ArticleMetadataConsumer articleMetadataConsumer() {
        return new ArticleMetadataConsumer();
    }

    @Bean
    @Scope("prototype")
    ArticleValidationConsumer articleValidationConsumer() {
        return new ArticleValidationConsumer();
    }

    @Bean
    @ConfigurationProperties("kiosk.pipeline.ArticleDedupConsumer")
    ArticleDedupConsumer articleDedupConsumer(){
        return new ArticleDedupConsumer();
    }


    //-- Startup
    @PostConstruct
    public void init() {
        if (autoStartPipeline) {
            startUrlExtractor(CONSUMER_THREADS);
            startHtmlDownloader(CONSUMER_THREADS);
            startContentExtractor(CONSUMER_THREADS);

            startArticleMetadataConsumers(CONSUMER_THREADS);
            startArticleValidationConsumers(CONSUMER_THREADS);

            startImageExtractorConsumers(2 * CONSUMER_THREADS);
            startImageThumbnailConsumers(2 * CONSUMER_THREADS);
            startImageMainConsumers(2 * CONSUMER_THREADS);

            startVideoExtractorConsumers(CONSUMER_THREADS);
        }
    }

    private void startUrlExtractor(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final UrlExtractorConsumer consumer = urlExtractorConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor(), maxThreadWait);
        }
    }

    private void startHtmlDownloader(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final HtmlDownloadConsumer consumer = htmlDownloadConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor(), maxThreadWait);
        }
    }

    private void startContentExtractor(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final ArticleContentExtractorConsumer consumer = articleContentExtractorConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor(), maxThreadWait);
        }
    }

    private void startImageExtractorConsumers(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final ImageExtractorConsumer consumer = imageExtractorConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor(), maxThreadWait);
        }
    }

    private void startImageThumbnailConsumers(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final ImageThumbnailConsumer consumer = imageThumbnailConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor(), maxThreadWait);
        }
    }

    private void startVideoExtractorConsumers(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final VideoExtractorConsumer consumer = videoExtractorConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor(), maxThreadWait);
        }
    }

    private void startImageMainConsumers(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final ImageMainConsumer consumer = imageMainConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor(), maxThreadWait);
        }
    }

    private void startArticleMetadataConsumers(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final ArticleMetadataConsumer consumer = articleMetadataConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor(), maxThreadWait);
        }
    }

    private void startArticleValidationConsumers(final int threadCount) {
        for (int i = 0; i < threadCount; i++) {
            final ArticleValidationConsumer consumer = articleValidationConsumer();
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor(), maxThreadWait);
        }
    }
}
