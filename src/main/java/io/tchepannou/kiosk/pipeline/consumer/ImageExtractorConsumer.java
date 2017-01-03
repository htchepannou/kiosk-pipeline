package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sns.AmazonSNS;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsSnsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Image;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.ImageRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.service.image.ImageExtractor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

@ConfigurationProperties("kiosk.pipeline.ImageExtractorConsumer")
@Transactional
public class ImageExtractorConsumer extends SqsSnsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageExtractorConsumer.class);

    @Autowired
    AmazonS3 s3;

    @Autowired
    AmazonSNS sns;

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ImageExtractor imageExtractor;

    @Autowired
    HttpService http;

    private String inputQueue;
    private String outputTopic;
    private String s3Bucket;
    private String s3Key;
    private String s3KeyHtml;

    //-- SqsConsumer
    @Override
    protected void consumeMessage(final String body) throws IOException {
        final long id = Long.parseLong(body.toString());
        final Link link = linkRepository.findOne(id);
        consume(link);
    }

    private void consume(final Link link) throws IOException {
        LOGGER.info("Extracting image from s3://{}/{}", s3Bucket, link.getS3Key());

        try (final S3Object s3Object = s3.getObject(s3Bucket, link.getS3Key())) {
            final String html = IOUtils.toString(s3Object.getObjectContent());
            final String url = imageExtractor.extract(html);
            if (!Strings.isNullOrEmpty(url)) {
                final Image img = download(url, link);
                if (img != null) {
                    imageRepository.save(img);
                    sns.publish(outputTopic, String.valueOf(img.getId()));
                }
            }
        }
    }

    private Image download(final String url, final Link link) throws IOException {

        LOGGER.info("Downloading {}", url);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final String contentType = http.get(url, out);
        if (contentType == null || !contentType.startsWith("image/")) {
            LOGGER.error("{} has invalid content-type: {}", url, contentType);
            return null;
        }

        // Store image
        final byte[] bytes = out.toByteArray();
        final BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(bytes));
        if (bimg == null) {
            LOGGER.error("{} is corrupted. Cannot load the image!", url);
            return null;
        }
        final String key = imageKey(url, link);
        final Image img = new Image();
        img.setLink(link);
        img.setUrl(url);
        img.setS3Key(key);
        img.setContentLength(bytes.length);
        img.setContentType(contentType);
        img.setWidth(bimg.getWidth());
        img.setHeight(bimg.getHeight());
        img.setType(Image.TYPE_ORIGINAL);

        // Store content
        LOGGER.info("Storing {} to s3://{}/{}", url, s3Bucket, key);
        final ObjectMetadata meta = createObjectMetadata(bytes.length, contentType);
        s3.putObject(s3Bucket, key, new ByteArrayInputStream(bytes), meta);

        return img;
    }

    private String imageKey(final String url, final Link link) throws IOException {
        final String key = link.getS3Key();
        final String filename = normazlieFilename(new URL(url).getFile());
        final String extension = Files.getFileExtension(filename);
        return s3Key + key.substring(s3KeyHtml.length(), key.length() - 4) + extension;
    }

    private ObjectMetadata createObjectMetadata(final int len, final String contentType) {
        final ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(len);
        meta.setContentType(contentType);
        return meta;
    }

    private String normazlieFilename(final String filename) {
        final int i = filename.indexOf('?');
        return i > 0 ? filename.substring(0, i) : filename;
    }

    //-- Getter/Setter
    public String getInputQueue() {
        return inputQueue;
    }

    public void setInputQueue(final String inputQueue) {
        this.inputQueue = inputQueue;
    }

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

    public String getS3KeyHtml() {
        return s3KeyHtml;
    }

    public void setS3KeyHtml(final String s3KeyHtml) {
        this.s3KeyHtml = s3KeyHtml;
    }

    public String getOutputTopic() {
        return outputTopic;
    }

    public void setOutputTopic(final String outputTopic) {
        this.outputTopic = outputTopic;
    }
}
