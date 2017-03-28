package io.tchepannou.kiosk.pipeline.config;

import io.tchepannou.kiosk.core.service.Consumer;
import io.tchepannou.kiosk.core.service.Delay;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.core.service.MessageQueueProcessor;
import io.tchepannou.kiosk.core.service.MessageQueueSet;
import io.tchepannou.kiosk.core.service.impl.ConstantDelay;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.service.UrlService;
import io.tchepannou.kiosk.pipeline.step.content.ContentConsumer;
import io.tchepannou.kiosk.pipeline.step.content.filter.AnchorFilter;
import io.tchepannou.kiosk.pipeline.step.content.filter.ContentExtractor;
import io.tchepannou.kiosk.pipeline.step.content.filter.ContentFilter;
import io.tchepannou.kiosk.pipeline.step.content.filter.HeadingOnlyFilter;
import io.tchepannou.kiosk.pipeline.step.content.filter.HtmlEntityFilter;
import io.tchepannou.kiosk.pipeline.step.content.filter.SanitizeFilter;
import io.tchepannou.kiosk.pipeline.step.content.filter.TrimFilter;
import io.tchepannou.kiosk.pipeline.step.download.DownloadConsumer;
import io.tchepannou.kiosk.pipeline.step.image.ImageConsumer;
import io.tchepannou.kiosk.pipeline.step.image.ThumbnailConsumer;
import io.tchepannou.kiosk.pipeline.step.metadata.MetadataConsumer;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilterSet;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleCountryFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleFeedFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleRegexFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleSuffixFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleVideoFilter;
import io.tchepannou.kiosk.pipeline.step.publish.PublishConsumer;
import io.tchepannou.kiosk.pipeline.step.url.FeedUrlProducer;
import io.tchepannou.kiosk.pipeline.step.url.UrlProducer;
import io.tchepannou.kiosk.pipeline.step.validation.ValidationConsumer;
import io.tchepannou.kiosk.pipeline.step.validation.Validator;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleShouldHaveContentRule;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleShouldHaveMinContentLengthRule;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleShouldHaveTitleRule;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleUrlShouldNotBeBlacklistedRule;
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
    UrlService urlService;

    @Autowired
    @Qualifier("UrlMessageQueue")
    MessageQueue urlMessageQueue;

    @Autowired
    @Qualifier("MetadataMessageQueue")
    MessageQueue metadataMessageQueue;

    @Autowired
    @Qualifier("ContentMessageQueue")
    MessageQueue contentMessageQueue;

    @Autowired
    @Qualifier("ValidationMessageQueue")
    MessageQueue validationMessageQueue;

    @Autowired
    @Qualifier("ImageMessageQueue")
    MessageQueue imageMessageQueue;

    @Autowired
    @Qualifier("ThumbnailMessageQueue")
    MessageQueue thumbnailMessageQueue;

    @Autowired
    @Qualifier("VideoMessageQueue")
    MessageQueue videoMessageQueue;

    @Autowired
    @Qualifier("PublishMessageQueue")
    MessageQueue publishMessageQueue;

    int workers;

    @PostConstruct
    public void run() {
        // Fetch URLs
        urlProducer().produce();

        // URL processing
        execute(downloadMessageQueueProcessor());
        execute(metadataMessageQueueProcessor());
        execute(contentMessageQueueProcessor());
        execute(validationMessageQueueProcessor());
        execute(imageMessageQueueProcessor());
        execute(thumbnailMessageQueueProcessor());
        execute(publishMessageQueueProcessor());
    }

    private void execute(final Runnable runnable) {
        for (int i = 0; i < workers; i++) {
            executor.execute(runnable);
        }
    }

    //-- Common
    @Bean
    Delay delay() {
        return new ConstantDelay(60000);
    }

    //-- Url
    @Bean
    UrlProducer urlProducer() {
        return new UrlProducer();
    }

    @Bean
    FeedUrlProducer feedUrlProducer() {
        return new FeedUrlProducer();
    }

    //-- Download
    @Bean
    MessageQueueProcessor downloadMessageQueueProcessor() {
        return new MessageQueueProcessor(
                urlMessageQueue,
                downloadConsumer(),
                delay()
        );
    }

    @Bean
    @ConfigurationProperties("kiosk.step.DownloadConsumer")
    Consumer downloadConsumer() {
        return new DownloadConsumer();
    }

    //-- Metadata
    @Bean
    MessageQueueProcessor metadataMessageQueueProcessor() {
        return new MessageQueueProcessor(
                metadataMessageQueue,
                metadataConsumer(),
                delay()
        );
    }

    @Bean
    @ConfigurationProperties("kiosk.step.MetadataConsumer")
    Consumer metadataConsumer() {
        return new MetadataConsumer();
    }

    @Bean
    TitleFilter metadataTitleFilter() {
        return new TitleFilterSet(Arrays.asList(
                new TitleRegexFilter(),
                new TitleCountryFilter(),
                new TitleFeedFilter(),
                new TitleVideoFilter(),

                new TitleSuffixFilter() /* SHOULD BE THE LAST!!! */
        ));
    }

    //-- Content
    @Bean
    MessageQueueProcessor contentMessageQueueProcessor() {
        return new MessageQueueProcessor(
                contentMessageQueue,
                contentConsumer(),
                delay()
        );
    }

    @Bean
    @ConfigurationProperties("kiosk.step.ContentConsumer")
    Consumer contentConsumer() {
        return new ContentConsumer();
    }

    @Bean
    ContentExtractor contentExtractor() {
        return new ContentExtractor(Arrays.asList(
                new SanitizeFilter(),
                new ContentFilter(100),
                new AnchorFilter(),
                new HeadingOnlyFilter(),
                new TrimFilter(),
                new HtmlEntityFilter()
        ));
    }

    //-- Validation
    @Bean
    MessageQueueProcessor validationMessageQueueProcessor() {
        return new MessageQueueProcessor(
                validationMessageQueue,
                validationConsumer(),
                delay()
        );
    }

    @Bean
    Consumer validationConsumer() {
        return new ValidationConsumer();
    }

    @Bean("ValidatedTopic")
    MessageQueue validationTopic() {
        return new MessageQueueSet(
                "validated",
                Arrays.asList(imageMessageQueue)
        );
    }

    @Bean
    Validator<Link> articleValidator() {
        return new Validator<>(
                Arrays.asList(
                        new ArticleShouldHaveContentRule(),
                        new ArticleShouldHaveMinContentLengthRule(100),
                        new ArticleShouldHaveTitleRule(),
                        new ArticleUrlShouldNotBeBlacklistedRule(urlService)
                )
        );
    }

    //-- Image
    @Bean
    MessageQueueProcessor imageMessageQueueProcessor() {
        return new MessageQueueProcessor(
                imageMessageQueue,
                imageConsumer(),
                delay()
        );
    }

    @Bean
    @ConfigurationProperties("kiosk.step.ImageConsumer")
    Consumer imageConsumer() {
        return new ImageConsumer();
    }

    //-- Thubmnail
    @Bean
    MessageQueueProcessor thumbnailMessageQueueProcessor() {
        return new MessageQueueProcessor(
                thumbnailMessageQueue,
                thumbnailConsumer(),
                delay()
        );
    }

    @Bean
    @ConfigurationProperties("kiosk.step.ThumbnailConsumer")
    Consumer thumbnailConsumer() {
        return new ThumbnailConsumer();
    }

    //-- Thubmnail
    @Bean
    MessageQueueProcessor publishMessageQueueProcessor() {
        return new MessageQueueProcessor(
                publishMessageQueue,
                publishConsumer(),
                delay()
        );
    }

    @Bean
    @ConfigurationProperties("kiosk.step.PublishConsumer")
    Consumer publishConsumer() {
        return new PublishConsumer();
    }

    //-- Getter/Setter
    public int getWorkers() {
        return workers;
    }

    public void setWorkers(final int workers) {
        this.workers = workers;
    }
}
