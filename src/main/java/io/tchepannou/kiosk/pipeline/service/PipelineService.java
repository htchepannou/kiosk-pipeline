package io.tchepannou.kiosk.pipeline.service;

import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;

public class PipelineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PipelineService.class);

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    @Qualifier("MetadataMessageQueue")
    MessageQueue metadataMessageQueue;

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

}
