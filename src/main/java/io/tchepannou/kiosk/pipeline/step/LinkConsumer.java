package io.tchepannou.kiosk.pipeline.step;

import io.tchepannou.kiosk.core.service.Consumer;
import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class LinkConsumer implements Consumer {
    @Autowired
    protected LinkRepository linkRepository;

    @Autowired
    protected FileRepository repository;

    abstract protected void consume(Link link) throws IOException;

    @Override
    public void consume(final String message) throws IOException {
        Link link = linkRepository.findOne(Long.parseLong(message));
        consume(link);
    }

    protected void push(final Link link, final MessageQueue queue) throws IOException {
        queue.push(String.valueOf(link.getId()));
    }

    protected final String getRawHtml(final Link link) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        repository.read(link.getS3Key(), out);

        return IOUtils.toString(new ByteArrayInputStream(out.toByteArray()), "utf-8");
    }

    protected Document getRawDocument(final Link link) throws IOException {
        final String html = getRawHtml(link);
        return Jsoup.parse(html);
    }


}
