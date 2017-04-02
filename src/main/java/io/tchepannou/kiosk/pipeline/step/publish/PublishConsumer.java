package io.tchepannou.kiosk.pipeline.step.publish;

import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkStatusEnum;
import io.tchepannou.kiosk.pipeline.step.AbstractLinkConsumer;

import javax.transaction.Transactional;
import java.io.IOException;

@Transactional
public class PublishConsumer extends AbstractLinkConsumer {
    @Override
    protected void consume(final Link link) throws IOException {
        if (link.isValid()) {
            link.setStatus(LinkStatusEnum.published);
        }
        linkRepository.save(link);
    }
}
