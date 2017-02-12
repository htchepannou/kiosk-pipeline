package io.tchepannou.kiosk.pipeline.config;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumerGroup;
import io.tchepannou.kiosk.pipeline.consumer.ArticleContentExtractorConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ArticleDedupConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ArticleMetadataConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ArticlePublishConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ArticleValidationConsumer;
import io.tchepannou.kiosk.pipeline.consumer.HtmlDownloadConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ImageExtractorConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ImageMainConsumer;
import io.tchepannou.kiosk.pipeline.consumer.ImageThumbnailConsumer;
import io.tchepannou.kiosk.pipeline.consumer.UrlExtractorConsumer;
import io.tchepannou.kiosk.pipeline.consumer.VideoExtractorConsumer;
import io.tchepannou.kiosk.pipeline.producer.FeedProducer;
import io.tchepannou.kiosk.pipeline.producer.PublishProducer;
import io.tchepannou.kiosk.pipeline.producer.SimilarityMatrixProducer;
import io.tchepannou.kiosk.pipeline.service.PipelineRunner;
import io.tchepannou.kiosk.pipeline.service.ThreadMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class PipelineConfiguration {
    @Autowired
    AmazonSQS sqs;

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @Value("${kiosk.pipeline.maxThreadWait}")
    int maxThreadWait;

    @Value("${kiosk.pipeline.workersPerConsumer}")
    int workersPerConsumer;


    //-- Beans
    @Bean
    ThreadMonitor threadMonitor() {
        return new ThreadMonitor();
    }

    @Bean
    PipelineRunner pipelineRunner() {
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
    @ConfigurationProperties("kiosk.pipeline.PublishProducer")
    PublishProducer publishProducer() {
        return new PublishProducer();
    }

    //-- Consumers
    @Bean("AquisitionConsumers")
    public SqsConsumerGroup aquisitionConsumers() {
        final SqsConsumerGroup group = new SqsConsumerGroup(sqs, threadMonitor(), applicationContext);
        group.add(UrlExtractorConsumer.class, 2* workersPerConsumer);
        group.add(HtmlDownloadConsumer.class, workersPerConsumer);

        group.add(ArticleContentExtractorConsumer.class, workersPerConsumer);
        group.add(ArticleMetadataConsumer.class, workersPerConsumer);
        group.add(ArticleValidationConsumer.class, workersPerConsumer);

        group.add(ImageExtractorConsumer.class, workersPerConsumer);
        group.add(ImageThumbnailConsumer.class, 2 * workersPerConsumer);
        group.add(ImageMainConsumer.class, 2 * workersPerConsumer);

        group.add(VideoExtractorConsumer.class, 2 * workersPerConsumer);

        return group;
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

    @Bean("DedupConsumers")
    SqsConsumerGroup dedupConsumers() {
        final SqsConsumerGroup group = new SqsConsumerGroup(sqs, threadMonitor(), applicationContext);
        group.add(ArticleDedupConsumer.class, workersPerConsumer);
        group.setMaxThreadWait(1 * SqsConsumerGroup.ONE_MINUTE);
        return group;
    }

    @Bean
    @Scope("prototype")
    @ConfigurationProperties("kiosk.pipeline.ArticleDedupConsumer")
    ArticleDedupConsumer articleDedupConsumer() {
        return new ArticleDedupConsumer();
    }

    @Bean("PublishConsumers")
    SqsConsumerGroup publishConsumers() {
        final SqsConsumerGroup group = new SqsConsumerGroup(sqs, threadMonitor(), applicationContext);
        group.add(ArticlePublishConsumer.class, workersPerConsumer);
        group.setMaxThreadWait(1 * SqsConsumerGroup.ONE_MINUTE);
        return group;
    }

    @Bean
    @Scope("prototype")
    @ConfigurationProperties("kiosk.pipeline.ArticlePublishConsumer")
    ArticlePublishConsumer articlePublishConsumer() {
        return new ArticlePublishConsumer();
    }
}
