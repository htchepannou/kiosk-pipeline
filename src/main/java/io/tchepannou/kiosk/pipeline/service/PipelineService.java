package io.tchepannou.kiosk.pipeline.service;

import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.core.service.MessageQueueProcessor;
import io.tchepannou.kiosk.core.service.ThreadCountDown;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTypeEnum;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.step.publish.PublishProducer;
import io.tchepannou.kiosk.pipeline.step.url.UrlProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

public class PipelineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineService.class);

    @Autowired
    Executor executor;

    @Autowired
    FileRepository fileRepository;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    ThreadCountDown threadCountDown;

    @Autowired
    ShutdownService shutdownService;

    @Autowired
    UrlProducer urlProducer;

    @Autowired
    PublishProducer publishProducer;

    @Autowired
    @Qualifier("MetadataMessageQueue")
    MessageQueue metadataMessageQueue;

    @Autowired
    @Qualifier("DownloadMessageQueueProcessor")
    MessageQueueProcessor downloadMessageQueueProcessor;

    @Autowired
    @Qualifier("MetadataMessageQueueProcessor")
    MessageQueueProcessor metadataMessageQueueProcessor;

    @Autowired
    @Qualifier("ContentMessageQueueProcessor")
    MessageQueueProcessor contentMessageQueueProcessor;

    @Autowired
    @Qualifier("ValidationMessageQueueProcessor")
    MessageQueueProcessor validationMessageQueueProcessor;

    @Autowired
    @Qualifier("ImageMessageQueueProcessor")
    MessageQueueProcessor imageMessageQueueProcessor;

    @Autowired
    @Qualifier("ThumbnailMessageQueueProcessor")
    MessageQueueProcessor thumbnailMessageQueueProcessor;

    @Autowired
    @Qualifier("VideoMessageQueueProcessor")
    MessageQueueProcessor videoMessageQueueProcessor;

    @Autowired
    @Qualifier("PublishMessageQueueProcessor")
    MessageQueueProcessor publishMessageQueueProcessor;

    boolean autostart;
    int workers;
    int maxDurationSeconds;
    String reprocessKey;

    //-- Public
    @Async
    public void reprocess() throws IOException {
        if (shouldReprocess()) {
            LOGGER.info("Reprocessing links");
            try {
                final Iterable<Feed> feeds = feedRepository.findAll();
                for (final Feed feed : feeds) {
                    if (feed.isActive()) {
                        reprocess(feed);
                    }
                }
            } finally {
                fileRepository.delete(reprocessKey);
            }
        }
    }

    @Async
    public void run() {
        // Schedule shutdown
        shutdownService.shutdown(maxDurationSeconds * 1000);

        // Process async
        if (!autostart) {
            return;
        }

        try {
            LOGGER.info("Starting pipeline");

            prePublish();
            publish();

            shutdownService.shutdownNow();
        } catch (final InterruptedException e) {
            LOGGER.warn("Interruped", e);
        }
    }

    //-- Private
    private void reprocess(final Feed feed) {
        final int limit = 200;
        for (int i = 0; ; i++) {
            final Pageable pageable = new PageRequest(i, limit);
            final List<Link> links = linkRepository.findByFeedAndType(feed, LinkTypeEnum.article, pageable);
            LOGGER.error("<{}> article(s) from <{}> to reprocess", links.size(), feed.getUrl());

            for (final Link link : links) {
                try {
                    metadataMessageQueue.push(String.valueOf(link.getId()));
                } catch (final IOException e) {
                    LOGGER.error("Unable to push <{}> to <{}>", link.getId(), metadataMessageQueue.getName());
                }
            }

            if (links.size() < limit) {
                break;
            }
        }

    }

    private boolean shouldReprocess() {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            fileRepository.read(reprocessKey, out);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    private void prePublish() throws InterruptedException {
        LOGGER.info("Processing URL");

        urlProducer.produce();
        execute(downloadMessageQueueProcessor);
        execute(metadataMessageQueueProcessor);
        execute(contentMessageQueueProcessor);
        execute(validationMessageQueueProcessor);
        execute(imageMessageQueueProcessor);
        execute(thumbnailMessageQueueProcessor);
        execute(videoMessageQueueProcessor);

        threadCountDown.await();
    }

    private void publish() throws InterruptedException {
        LOGGER.info("Publishing Articles");

        publishProducer.produce();
        execute(publishMessageQueueProcessor);
        threadCountDown.await();
    }

    private void execute(final Runnable runnable) {
        for (int i = 0; i < workers; i++) {
            executor.execute(runnable);
        }
    }

    //-- Getter/Setter
    public boolean isAutostart() {
        return autostart;
    }

    public void setAutostart(final boolean autostart) {
        this.autostart = autostart;
    }

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

    public String getReprocessKey() {
        return reprocessKey;
    }

    public void setReprocessKey(final String reprocessKey) {
        this.reprocessKey = reprocessKey;
    }
}
