package io.tchepannou.kiosk.pipeline.producer;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class UrlProducerRunnable implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlProducerRunnable.class);

    private final Feed feed;

    private final UrlService urlService;

    private final LinkRepository linkRepository;

    private final AmazonSQS sqs;

    private final String outputQueue;

    public UrlProducerRunnable(
            final Feed feed,
            final UrlService urlService,
            final LinkRepository linkRepository,
            final AmazonSQS sqs,
            final String outputQueue
    ) {
        this.feed = feed;
        this.urlService = urlService;
        this.linkRepository = linkRepository;
        this.sqs = sqs;
        this.outputQueue = outputQueue;
    }

    @Override
    public void run() {
        final String feedName = feed.getName();
        final String feedUrl = urlService.normalize(feed.getUrl());
        LOGGER.info("Extract URLs from {} @ {}", feedName, feedUrl);
        try {

            final Collection<String> feedUrls = getFeedUrls();
            LOGGER.info("{} has {} urls", feedName, feedUrls.size());

            final Collection<String> urls = urlService.extractUrls(feed);
            LOGGER.info("{} has {} urls", feedUrl, urls.size());

            for (final String url : urls) {
                if (url.equals(feedUrl)) {
                    LOGGER.info("SKIP {} is homepage", url);
                } else if (feedUrls.contains(url)) {
                    LOGGER.info("SKIP {} has already been downloaded", url);
                } else if (urlService.isBlacklisted(url)) {
                    LOGGER.info("SKIP {} is blacklisted", url);
                } else {
                    LOGGER.info("Sending message <{}> to queue: {}", url, outputQueue);
                    sqs.sendMessage(outputQueue, url);
                }
            }

            LOGGER.info("DONE with {}", feedName);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error when extracting URLs from {}", feed.getUrl(), e);
        }
    }

    private Collection<String> getFeedUrls() {
        final int limit = 200;
        final Set<String> result = new HashSet<>();
        for (int page = 0; page < 50; page++) {

            final Pageable pageable = new PageRequest(page, limit, Sort.Direction.DESC, "id");
            final Iterable<Link> links = linkRepository.findByFeed(feed, pageable);
            int count = 0;
            for (final Link link : links){
                final String url = urlService.normalize(link.getUrl());
                result.add(url);
                count++;
            }

            if (count < limit){
                break;
            }
        }
        return result;
    }
}
