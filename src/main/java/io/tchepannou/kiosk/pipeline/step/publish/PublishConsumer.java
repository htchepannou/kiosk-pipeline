package io.tchepannou.kiosk.pipeline.step.publish;

import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkStatusEnum;
import io.tchepannou.kiosk.pipeline.step.AbstractLinkConsumer;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.Clock;
import java.util.Date;

@Transactional
public class PublishConsumer extends AbstractLinkConsumer {
    @Autowired
    Clock clock;

    @Override
    protected void consume(final Link link) throws IOException {
        if (link.isValid()) {
            link.setStatus(LinkStatusEnum.published);
            link.setPublishedDate(new Date(clock.millis()));
        }
        linkRepository.save(link);
    }
}
