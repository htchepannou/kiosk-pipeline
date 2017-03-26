package io.tchepannou.kiosk.pipeline.step.url;

import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.Collection;

public class FeedUrlProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FeedUrlProducer.class);

    @Autowired
    @Qualifier("UrlMessageQueue")
    private MessageQueue output;

    @Autowired
    private UrlService urlService;

    @Autowired
    private LinkRepository linkRepository;


    @Async
    public void produce (final Feed feed) throws IOException {
        final Collection<String> urls = urlService.extractUrls(feed);
        LOGGER.info("{} URLs found for {}", urls.size(), feed.getUrl());

        for (final String url : urls) {
            if (shouldConsume(url, feed)){
                output.push(url);
            }
        }
    }

    private boolean shouldConsume (final String url, final Feed feed){
        if (url.equals(feed.getUrl())) {
            LOGGER.info("SKIP - Homepage: {}", url);
            return false;
        } else if (urlService.isBlacklisted(url)) {
            LOGGER.info("SKIP - Blacklisted: {}", url);
            return false;
        } else {
            final String hash = Link.hash(url);
            if (linkRepository.findByUrlHash(hash) != null){
                LOGGER.info("SKIP - Already downloaded: {}", url);
                return false;
            }
        }

        return true;
    }

}
