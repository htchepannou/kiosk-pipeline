package io.tchepannou.kiosk.pipeline.config;

import io.tchepannou.kiosk.core.service.Consumer;
import io.tchepannou.kiosk.core.service.Delay;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.core.service.MessageQueueProcessor;
import io.tchepannou.kiosk.core.service.MessageQueueSet;
import io.tchepannou.kiosk.core.service.Producer;
import io.tchepannou.kiosk.core.service.ThreadCountDown;
import io.tchepannou.kiosk.core.service.impl.ConstantDelay;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.service.UrlService;
import io.tchepannou.kiosk.pipeline.step.content.ContentConsumer;
import io.tchepannou.kiosk.pipeline.step.content.filter.AnchorFilter;
import io.tchepannou.kiosk.pipeline.step.content.filter.ContentExtractor;
import io.tchepannou.kiosk.pipeline.step.content.filter.ContentFilter;
import io.tchepannou.kiosk.pipeline.step.content.filter.HeadingOnlyFilter;
import io.tchepannou.kiosk.pipeline.step.content.filter.HtmlEntityFilter;
import io.tchepannou.kiosk.pipeline.step.content.filter.IdFilter;
import io.tchepannou.kiosk.pipeline.step.content.filter.SanitizeFilter;
import io.tchepannou.kiosk.pipeline.step.content.filter.TrimFilter;
import io.tchepannou.kiosk.pipeline.step.download.DownloadConsumer;
import io.tchepannou.kiosk.pipeline.step.image.ImageConsumer;
import io.tchepannou.kiosk.pipeline.step.image.ThumbnailConsumer;
import io.tchepannou.kiosk.pipeline.step.metadata.HtmlTagExtractor;
import io.tchepannou.kiosk.pipeline.step.metadata.MetadataConsumer;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.TitleFilterSet;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleCountryFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleFeedFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleRegexFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleSuffixFilter;
import io.tchepannou.kiosk.pipeline.step.metadata.filter.TitleVideoFilter;
import io.tchepannou.kiosk.pipeline.step.publish.PublishConsumer;
import io.tchepannou.kiosk.pipeline.step.publish.PublishProducer;
import io.tchepannou.kiosk.pipeline.step.url.FeedUrlProducer;
import io.tchepannou.kiosk.pipeline.step.url.UrlProducer;
import io.tchepannou.kiosk.pipeline.step.validation.ValidationConsumer;
import io.tchepannou.kiosk.pipeline.step.validation.Validator;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleShouldHaveContentRule;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleShouldHaveMinContentLengthRule;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleShouldHaveTitleRule;
import io.tchepannou.kiosk.pipeline.step.validation.rules.ArticleUrlShouldNotBeBlacklistedRule;
import io.tchepannou.kiosk.pipeline.step.video.VideoConsumer;
import io.tchepannou.kiosk.pipeline.step.video.providers.YouTube;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

@Configuration
@ConfigurationProperties("kiosk.step")
public class PipelineConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineConfiguration.class);

    @Autowired
    ThreadPoolTaskExecutor executor;

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

    boolean autostart;
    int workers;
    int maxDurationSeconds;

    @PostConstruct
    public void run() throws InterruptedException {
        LOGGER.info("Starting pipeline");

        // Schedule shutdown
        shutdown(maxDurationSeconds * 1000);

        // Process async
        if (!autostart) {
            return;
        }
        executor.execute(() -> {
            try {
                prePublish();
                publish();

                shutdown(0);
            } catch (InterruptedException e) {
                LOGGER.warn("Interruped", e);
            }
        });
    }

    private void shutdown(final int sleepMillis) {
        executor.execute(() -> {
            try {
                if (sleepMillis > 0) {
                    Thread.sleep(sleepMillis);
                }

                LOGGER.info("Shutting down...");
                System.exit(0);
            } catch (final InterruptedException e) {

            }
        });
    }

    private void prePublish() throws InterruptedException {
        LOGGER.info("Processing URL");

        urlProducer().produce();
        execute(downloadMessageQueueProcessor());
        execute(metadataMessageQueueProcessor());
        execute(contentMessageQueueProcessor());
        execute(validationMessageQueueProcessor());
        execute(imageMessageQueueProcessor());
        execute(thumbnailMessageQueueProcessor());
        execute(videoMessageQueueProcessor());

        threadCountDown().await();
    }

    private void publish() throws InterruptedException {
        LOGGER.info("Publishing Articles");

        publishProducer().produce();
        execute(publishMessageQueueProcessor());
        threadCountDown().await();
    }

    private void execute(final Runnable runnable) {
        for (int i = 0; i < workers; i++) {
            executor.execute(runnable);
        }
    }

    @Bean
    CountDownLatch countDownLatch() {
        return new CountDownLatch(7);
    }

    //-- Common
    @Bean
    @ConfigurationProperties("kiosk.step.Delay")
    Delay delay() {
        return new ConstantDelay();
    }

    @Bean
    ThreadCountDown threadCountDown() {
        return new ThreadCountDown();
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
                delay(),
                threadCountDown()
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
                delay(),
                threadCountDown()
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

    @Bean
    HtmlTagExtractor htmlTagExtractor(){
        return new HtmlTagExtractor();
    }


    //-- Content
    @Bean
    MessageQueueProcessor contentMessageQueueProcessor() {
        return new MessageQueueProcessor(
                contentMessageQueue,
                contentConsumer(),
                delay(),
                threadCountDown()
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
                new IdFilter(),
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
                delay(),
                threadCountDown()
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
                Arrays.asList(imageMessageQueue, videoMessageQueue)
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

    //-- Video
    @Bean
    MessageQueueProcessor videoMessageQueueProcessor() {
        return new MessageQueueProcessor(
                videoMessageQueue,
                videoConsumer(),
                delay(),
                threadCountDown()
        );
    }

    @Bean
    Consumer videoConsumer() {
        final VideoConsumer consumer = new VideoConsumer();
        consumer.setProviders(Arrays.asList(
                youTube()
        ));
        return consumer;
    }

    @Bean
    @ConfigurationProperties("kiosk.step.VideoConsumer.providers.youtube")
    YouTube youTube() {
        return new YouTube();
    }

    //-- Image
    @Bean
    MessageQueueProcessor imageMessageQueueProcessor() {
        return new MessageQueueProcessor(
                imageMessageQueue,
                imageConsumer(),
                delay(),
                threadCountDown()
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
                delay(),
                threadCountDown()
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
                delay(),
                threadCountDown()
        );
    }

    @Bean
    @ConfigurationProperties("kiosk.step.PublishConsumer")
    Consumer publishConsumer() {
        return new PublishConsumer();
    }

    @Bean
    Producer publishProducer() {
        return new PublishProducer();
    }

    //-- Getter/Setter
    public int getWorkers() {
        return workers;
    }

    public void setWorkers(final int workers) {
        this.workers = workers;
    }

    public int getMaxDurationSeconds() {
        return maxDurationSeconds;
    }

    public void setMaxDurationSeconds(final int maxDurationSeconds) {
        this.maxDurationSeconds = maxDurationSeconds;
    }

    public boolean isAutostart() {
        return autostart;
    }

    public void setAutostart(final boolean autostart) {
        this.autostart = autostart;
    }
}
