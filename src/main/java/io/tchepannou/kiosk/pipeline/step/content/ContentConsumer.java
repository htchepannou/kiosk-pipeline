package io.tchepannou.kiosk.pipeline.step.content;

import io.tchepannou.kiosk.core.nlp.language.LanguageDetector;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.step.AbstractLinkConsumer;
import io.tchepannou.kiosk.pipeline.step.content.filter.ContentExtractor;
import org.flywaydb.core.internal.util.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Transactional
public class ContentConsumer extends AbstractLinkConsumer {
    @Autowired
    @Qualifier("ValidationMessageQueue")
    MessageQueue queue;

    @Autowired
    ContentExtractor extractor;

    @Autowired
    LanguageDetector languageDetector;

    private String rawFolder;
    private String contentFolder;
    private int defaultSummaryMaxLength;

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
        final String text = Jsoup.parse(html).text();

        link.setContentKey(key);
        link.setContentLength(text.length());
        link.setContentType("text/html");

        if (link.getSummary() == null){
            final String summary = StringUtils.left(text, defaultSummaryMaxLength);
            link.setSummary(summary);
        }

        if (link.getLanguage() == null){
            final String ltext = link.getTitle() + "\n" + link.getSummary();
            link.setLanguage(languageDetector.detect(ltext));
        }

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

    public int getDefaultSummaryMaxLength() {
        return defaultSummaryMaxLength;
    }

    public void setDefaultSummaryMaxLength(final int defaultSummaryMaxLength) {
        this.defaultSummaryMaxLength = defaultSummaryMaxLength;
    }
}
