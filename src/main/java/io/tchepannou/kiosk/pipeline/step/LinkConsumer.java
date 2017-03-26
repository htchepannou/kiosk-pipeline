package io.tchepannou.kiosk.pipeline.step;

import io.tchepannou.kiosk.core.service.Consumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public abstract class LinkConsumer implements Consumer {
    @Autowired
    protected LinkRepository linkRepository;

    abstract protected void consume(Link link) throws IOException;

    @Override
    public void consume(final String message) throws IOException {
        Link link = linkRepository.findOne(Long.parseLong(message));
        consume(link);
    }
}
