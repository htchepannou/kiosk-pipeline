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
import io.tchepannou.kiosk.pipeline.consumer.VideoExtractorConsumer;
import io.tchepannou.kiosk.pipeline.producer.PublishProducer;
import io.tchepannou.kiosk.pipeline.producer.SimilarityMatrixProducer;
import io.tchepannou.kiosk.pipeline.producer.UrlProducer;
import io.tchepannou.kiosk.pipeline.service.aws.AwsPipelineRunner;
import io.tchepannou.kiosk.pipeline.service.ThreadMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Configuration
@Profile("!local")
@Deprecated
public class AwsPipelineConfiguration {
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
    AwsPipelineRunner pipelineRunner() {
        return new AwsPipelineRunner();
    }

    //-- Producer
    @Bean
    @ConfigurationProperties("kiosk.pipeline.UrlProducer")
    UrlProducer urlProducer() {
        return new UrlProducer();
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
        group.add(HtmlDownloadConsumer.class, workersPerConsumer);

        group.add(ArticleContentExtractorConsumer.class, workersPerConsumer);
        group.add(ArticleMetadataConsumer.class, workersPerConsumer);
        group.add(ArticleValidationConsumer.class, workersPerConsumer);

        group.add(ImageExtractorConsumer.class, workersPerConsumer);
//        group.add(ImageThumbnailConsumer.class, 2 * workersPerConsumer);
        group.add(ImageMainConsumer.class, workersPerConsumer);

        group.add(VideoExtractorConsumer.class, workersPerConsumer);

        return group;
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
