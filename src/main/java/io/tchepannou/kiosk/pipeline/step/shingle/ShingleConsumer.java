package io.tchepannou.kiosk.pipeline.step.shingle;

import com.google.common.io.Files;
import io.tchepannou.kiosk.core.nlp.tokenizer.impl.BasicTokenizer;
import io.tchepannou.kiosk.core.nlp.tokenizer.impl.FragmentTokenizer;
import io.tchepannou.kiosk.core.nlp.tokenizer.impl.NGramTokenizer;
import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.step.AbstractLinkConsumer;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Transactional
public class ShingleConsumer extends AbstractLinkConsumer {

    @Autowired
    @Qualifier("ShingleMessageQueue")
    MessageQueue queue;

    @Autowired
    FileRepository repository;

    @Autowired
    LinkRepository linkRepository;

    private int maxShingles;
    private int shingleSize;

    String contentFolder;
    String shingleFolder;

    @Override
    protected void consume(final Link link) throws IOException {
        final Document doc = getRawDocument(link);

        final List<String> shingles = extractShingles(doc);

        store(link, shingles);
    }

    private List<String> extractShingles (final Document doc){

        // Merge the fragments
        final FragmentTokenizer fragmentTokenizer = new FragmentTokenizer(new BasicTokenizer(doc.text()));
        final List<String> fragments = new ArrayList<>();
        while (true) {
            final String fragment = fragmentTokenizer.nextToken();
            if (fragment == null) {
                break;
            }

            fragments.add(fragment);
        }

        // Extract the shingles
        final String text = String.join(" ", fragments);
        final List<String> shingles = new ArrayList<>();
        final NGramTokenizer tokenizer = new NGramTokenizer(shingleSize, shingleSize, new BasicTokenizer(text));
        for (int count=maxShingles ; count>0 ; count--) {
            final String shingle = tokenizer.nextToken();
            if (shingle == null){
                break;
            }
            shingles.add(shingle);
        }

        return shingles;
    }

    private void store (final Link link, final List<String> shingles) throws IOException {
        // Store into filesystem
        final String text = String.join("\n", shingles).trim();
        final String key = shingleKey(link);
        repository.write(key, new ByteArrayInputStream(text.getBytes()));

        // Store into DB
        link.setShingleKey(key);
        linkRepository.save(link);
    }

    private String shingleKey(final Link link) {
        final String key = link.getContentKey();
        final String extension = Files.getFileExtension(key);
        return shingleFolder + key.substring(contentFolder.length(), key.length() - extension.length()) + "txt";
    }

    public int getMaxShingles() {
        return maxShingles;
    }

    public void setMaxShingles(final int maxShingles) {
        this.maxShingles = maxShingles;
    }

    public int getShingleSize() {
        return shingleSize;
    }

    public void setShingleSize(final int shingleSize) {
        this.shingleSize = shingleSize;
    }

    public String getContentFolder() {
        return contentFolder;
    }

    public void setContentFolder(final String contentFolder) {
        this.contentFolder = contentFolder;
    }

    public String getShingleFolder() {
        return shingleFolder;
    }

    public void setShingleFolder(final String shingleFolder) {
        this.shingleFolder = shingleFolder;
    }
}
