package io.tchepannou.kiosk.pipeline.step.publish;

import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.core.service.Producer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkStatusEnum;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.List;

public class PublishProducer implements Producer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishProducer.class);

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    @Qualifier("PublishMessageQueue")
    MessageQueue queue;

    int limit = 200;

    @Override
    public void produce() {
        for (int i = 0; ; i++) {
            final Pageable pageable = new PageRequest(i, limit, Sort.Direction.ASC, "id");
            final List<Link> links = linkRepository.findByStatus(LinkStatusEnum.valid, pageable);

            for (final Link link : links) {
                try {
                    queue.push(String.valueOf(link.getId()));
                } catch (final IOException e) {
                    LOGGER.error("Unable to push <{}> to <{}>", link.getId(), queue.getName());
                }
            }

            if (links.size() < limit) {
                break;
            }
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }
}
