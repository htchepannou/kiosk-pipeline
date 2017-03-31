package io.tchepannou.kiosk.pipeline.step.download;

import io.tchepannou.kiosk.core.service.Consumer;
import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.service.InvalidContentTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;

@Transactional
public class DownloadConsumer implements Consumer {
    public static final Logger LOGGER = LoggerFactory.getLogger(DownloadConsumer.class);

    @Autowired
    HttpService http;

    @Autowired
    FileRepository repository;

    @Autowired
    @Qualifier("MetadataMessageQueue")
    MessageQueue queue;

    @Autowired
    Clock clock;

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    FeedRepository feedRepository;

    String folder;

    private Iterable<Feed> feeds;

    @PostConstruct
    public void init() {
        feeds = feedRepository.findAll();
    }

    @Override
    public void consume(final String url) throws IOException {

        try {

            // Download
            final byte[] bytes = download(url);

            // Store
            final String key = generateKey(url);
            repository.write(key, new ByteArrayInputStream(bytes));

            // Create Link
            final Link link = createLink(url, key);

            // Publish
            queue.push(String.valueOf(link.getId()));

        } catch (final DataIntegrityViolationException e) {

            LOGGER.error("{} already downloaded", url, e);

        } catch (final InvalidContentTypeException e) {

            LOGGER.error("{} not valid HTML", url, e);

        }
    }


    //-- Getter/Setter
    public String getFolder() {
        return folder;
    }

    public void setFolder(final String folder) {
        this.folder = folder;
    }


    //-- Private
    final byte[] download (final String url) throws IOException {
        LOGGER.info("Downloading {}", url);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        http.getHtml(url, out);
        return out.toByteArray();
    }

    private String generateKey(final String url) {
        final String id = Link.hash(url);
        final DateFormat fmt = new SimpleDateFormat("yyyy/MM/dd/HH");
        final Date now = new Date(clock.millis());
        return String.format("%s/%s/%s.html", folder, fmt.format(now), id);
    }

    private Link createLink(final String url, final String key) {
        final Feed feed = findFeed(url);
        final Link link = new Link();
        link.setUrl(url);
        link.setUrlHash(Link.hash(url));
        link.setS3Key(key);
        link.setFeed(feed);

        linkRepository.save(link);

        return link;
    }

    private Feed findFeed(final String url) {
        for (final Feed feed : feeds) {
            if (url.startsWith(feed.getUrl())) {
                return feed;
            }
        }
        return null;
    }

}
