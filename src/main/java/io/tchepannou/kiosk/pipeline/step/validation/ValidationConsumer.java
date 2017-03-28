package io.tchepannou.kiosk.pipeline.step.validation;

import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.step.AbstractLinkConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.transaction.Transactional;
import java.io.IOException;

@Transactional
public class ValidationConsumer extends AbstractLinkConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationConsumer.class);

    @Autowired
    private Validator validator;

    @Autowired
    @Qualifier("ValidatedTopic")
    private MessageQueue queue;

    //-- SqsConsumer
    @Override
    public void consume(final Link link) throws IOException {
        final Validation validation = validator.validate(link);

        if (validation.isSuccess()) {
            LOGGER.info("{} is valid", link.getUrl());
            link.setValid(true);
            link.setInvalidReason(null);
            push(link, queue);
        } else {
            LOGGER.info("{} is invalid. reason={}", link.getUrl(), validation.getReason());
            link.setValid(false);
            link.setInvalidReason(validation.getReason());
        }
        linkRepository.save(link);
    }

}
