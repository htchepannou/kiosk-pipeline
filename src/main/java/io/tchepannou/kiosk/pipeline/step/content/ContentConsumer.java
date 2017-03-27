package io.tchepannou.kiosk.pipeline.step.content;

import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.step.LinkConsumer;
import io.tchepannou.kiosk.pipeline.step.content.filter.ContentExtractor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Transactional
public class ContentConsumer extends LinkConsumer {
    @Autowired
    FileRepository repository;

    @Autowired
    @Qualifier("ValidationMessageQueue")
    MessageQueue queue;

    @Autowired
    ContentExtractor extractor;

    private String rawFolder;
    private String contentFolder;

    @Override
    protected void consume(final Link link) throws IOException {
        // Extract content
        final String html = getRawHtml(link);
        final String xhtml = extractor.extract(html);

        // Store content
        final String key = contentKey(link);
        store(key, xhtml);

        // Update DB
        updateLink(link, key, xhtml);

        // Next
        push(link, queue);
    }

    //-- Private
    private String getRawHtml(final Link link) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        repository.read(link.getS3Key(), out);

        return IOUtils.toString(new ByteArrayInputStream(out.toByteArray()), "utf-8");
    }

    private void store (final String key, final String xhtml) throws IOException {
        final byte[] bytes = xhtml.getBytes("utf-8");
        final InputStream in = new ByteArrayInputStream(bytes);
        repository.write(key, in);

    }

    private String contentKey(final Link link) {
        final String key = link.getS3Key();
        return contentFolder + key.substring(rawFolder.length());
    }

    private void updateLink(final Link link, final String key, final String html){
        link.setContentKey(key);
        link.setContentLength(html.length());
        link.setContentType("text/html");
        linkRepository.save(link);
    }

    //-- Getter/Setter
    public String getRawFolder() {
        return rawFolder;
    }

    public void setRawFolder(final String rawFolder) {
        this.rawFolder = rawFolder;
    }

    public String getContentFolder() {
        return contentFolder;
    }

    public void setContentFolder(final String contentFolder) {
        this.contentFolder = contentFolder;
    }
}
