package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.sns.AmazonSNS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.service.InvalidContentTypeException;
import io.tchepannou.kiosk.pipeline.service.UrlService;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
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

@ConfigurationProperties("kiosk.pipeline.HtmlDownloadConsumer")
@Transactional
public class HtmlDownloadConsumer implements SqsConsumer {
    public static final Logger LOGGER = LoggerFactory.getLogger(HtmlDownloadConsumer.class);

    @Autowired
    AmazonS3 s3;

    @Autowired
    AmazonSNS sns;

    @Autowired
    HttpService http;

    @Autowired
    Clock clock;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    UrlService urlService;

    private String inputQueue;
    private String outputTopic;
    private String s3Bucket;
    private String s3Key;
    private Iterable<Feed> feeds;

    @PostConstruct
    public void init() {
        feeds = feedRepository.findAll();
    }

    @Override
    public void consume(final String body) throws IOException {
        if (alreadyDownloaded(body) || isBlacklisted(body)) {
            return;
        }
        try {
            // Feed
            final Feed feed = findFeed(body);
            if (feed == null) {
                LOGGER.error("Bad URL - No feed associated with {}", body);
                return;
            }

            // Download
            LOGGER.info("Downloading {}", body);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            http.getHtml(body, out);

            // Store
            final String id = DigestUtils.md5Hex(body);
            final String s3Key = generateKey(id);
            final byte[] bytes = out.toByteArray();
            final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            final ObjectMetadata meta = createObjectMetadata(bytes.length);

            LOGGER.info("Storing {} to s3://{}/{}", body, s3Bucket, s3Key);
            s3.putObject(s3Bucket, s3Key, in, meta);

            downloaded(body, s3Key, feed);
        } catch (final DataIntegrityViolationException e) {
            LOGGER.warn("{} already downloaded", body);
        } catch (final InvalidContentTypeException e) {
            LOGGER.warn("{} not valid HTML", body);
        }
    }

    //-- Private
    private String generateKey(final String id) {
        final DateFormat fmt = new SimpleDateFormat("yyyy/MM/dd/HH");
        final Date now = new Date(clock.millis());
        return String.format("%s/%s/%s.html", s3Key, fmt.format(now), id);
    }

    private ObjectMetadata createObjectMetadata(final int len) {
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("text/html");
        metadata.setContentLength(len);
        return metadata;
    }

    private boolean alreadyDownloaded(final String url) {
        final String keyhash = Link.hash(url);
        return linkRepository.findByUrlHash(keyhash) != null;
    }

    private boolean isBlacklisted(final String url) {
        return urlService.isBlacklisted(url);
    }

    private void downloaded(final String url, final String s3Key, final Feed feed) {
        final Link link = new Link();
        link.setUrl(url);
        link.setUrlHash(Link.hash(url));
        link.setS3Key(s3Key);
        link.setFeed(feed);
        linkRepository.save(link);

        LOGGER.info("Sending {} to {}", link.getId(), outputTopic);
        sns.publish(outputTopic, String.valueOf(link.getId()));
    }

    private Feed findFeed(final String url) {
        for (final Feed feed : feeds) {
            if (url.startsWith(feed.getUrl())) {
                return feed;
            }
        }
        return null;
    }

    //-- Getter/Setter

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(final String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(final String s3Key) {
        this.s3Key = s3Key;
    }

    public String getInputQueue() {
        return inputQueue;
    }

    public void setInputQueue(final String inputQueue) {
        this.inputQueue = inputQueue;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public void setOutputTopic(final String outputTopic) {
        this.outputTopic = outputTopic;
    }
}
