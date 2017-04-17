package io.tchepannou.kiosk.pipeline.config;

import io.tchepannou.kiosk.core.nlp.filter.HyphenFilter;
import io.tchepannou.kiosk.core.nlp.filter.LowercaseTextFilter;
import io.tchepannou.kiosk.core.nlp.filter.TextFilter;
import io.tchepannou.kiosk.core.nlp.filter.TextFilterSet;
import io.tchepannou.kiosk.core.nlp.filter.UnaccentTextFilter;
import io.tchepannou.kiosk.core.nlp.filter.WhitespaceTextFilter;
import io.tchepannou.kiosk.core.service.Consumer;
import io.tchepannou.kiosk.core.service.Delay;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.core.service.MessageQueueProcessor;
import io.tchepannou.kiosk.core.service.MessageQueueSet;
import io.tchepannou.kiosk.core.service.ThreadCountDown;
import io.tchepannou.kiosk.core.service.impl.ConstantDelay;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.service.PipelineService;
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
import io.tchepannou.kiosk.pipeline.step.tag.TagConsumer;
import io.tchepannou.kiosk.pipeline.step.tag.TagService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;

@Configuration
@ConfigurationProperties("kiosk.step")
public class PipelineConfiguration {
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

    @Autowired
    @Qualifier("TagMessageQueue")
    MessageQueue tagMessageQueue;

    @PostConstruct
    public void run() throws IOException {
        pipelineService().reprocess();
        pipelineService().run();
    }

    //-- Commons
    @Bean
    @ConfigurationProperties("kiosk.service.PipelineService")
    PipelineService pipelineService() {
        return new PipelineService();
    }

    @Bean
    @ConfigurationProperties("kiosk.service.Delay")
    @Scope("prototype")
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
    @Bean(name = "DownloadMessageQueueProcessor")
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
    @Bean(name = "MetadataMessageQueueProcessor")
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

    //-- Content
    @Bean(name = "ContentMessageQueueProcessor")
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
    @Bean(name = "ValidationMessageQueueProcessor")
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
                Arrays.asList(imageMessageQueue, videoMessageQueue, tagMessageQueue)
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

    //-- Tag
    @Bean(name = "TagMessageQueueProcessor")
    MessageQueueProcessor tagMessageQueueProcessor() {
        return new MessageQueueProcessor(
                tagMessageQueue,
                tagConsumer(),
                delay(),
                threadCountDown()
        );
    }

    @Bean
    Consumer tagConsumer() {
        return new TagConsumer();
    }

    @Bean
    public TagService tagService() {
        return new TagService();
    }

    @Bean("TagTextFilter")
    TextFilter tagTextFilter() {
        return new TextFilterSet(Arrays.asList(
                new UnaccentTextFilter(),
                new HyphenFilter(),
                new LowercaseTextFilter(),
                new WhitespaceTextFilter()
        ));
    }

    //-- Video
    @Bean(name = "VideoMessageQueueProcessor")
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
        return new VideoConsumer(Arrays.asList(
                youTube()
        ));
    }

    @Bean
    @ConfigurationProperties("kiosk.step.VideoConsumer.providers.youtube")
    YouTube youTube() {
        return new YouTube();
    }

    //-- Image
    @Bean(name = "ImageMessageQueueProcessor")
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
    @Bean(name = "ThumbnailMessageQueueProcessor")
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
    @Bean(name = "PublishMessageQueueProcessor")
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
    PublishProducer publishProducer() {
        return new PublishProducer();
    }
}
