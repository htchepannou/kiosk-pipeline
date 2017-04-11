package io.tchepannou.kiosk.pipeline.service;

import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.core.service.MessageQueueProcessor;
import io.tchepannou.kiosk.core.service.ThreadCountDown;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
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

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

public class PipelineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineService.class);

    @Autowired
    UrlProducer urlProducer;

    @Autowired
    PublishProducer publishProducer;

    @Autowired
    Executor executor;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    ThreadCountDown threadCountDown;

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

    //-- Public
    public void reprocess(final long feedId) {
        final Feed feed = feedRepository.findOne(feedId);
        final int limit = 200;
        for (int i = 0; ; i++) {
            final Pageable pageable = new PageRequest(i, limit);
            final List<Link> links = linkRepository.findByFeed(feed, pageable);

            for (final Link link : links) {
                try {
                    LOGGER.error("Pushing <{}> to <{}>", link.getId(), metadataMessageQueue.getName());
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
}
