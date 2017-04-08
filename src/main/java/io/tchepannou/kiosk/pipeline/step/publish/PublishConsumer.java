package io.tchepannou.kiosk.pipeline.step.publish;

import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkStatusEnum;
import io.tchepannou.kiosk.pipeline.step.AbstractLinkConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.io.IOException;

@Transactional
public class PublishConsumer extends AbstractLinkConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishConsumer.class);

    @Override
    protected void consume(final Link link) throws IOException {
        if (link.isValid()) {
            LOGGER.info("Publishing <{}>", link.getId());
            link.setStatus(LinkStatusEnum.published);
        }
        linkRepository.save(link);
    }
}
