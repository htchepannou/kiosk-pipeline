package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.io.Files;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Image;
import io.tchepannou.kiosk.pipeline.persistence.repository.ImageRepository;
import io.tchepannou.kiosk.pipeline.service.image.ImageProcessorService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@ConfigurationProperties("kiosk.pipeline.ImageThumbnailConsumer")
@Transactional
public class ImageThumbnailConsumer implements SqsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageThumbnailConsumer.class);

    @Autowired
    AmazonS3 s3;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ImageProcessorService imageProcessorService;

    private String inputQueue;
    private String s3Bucket;
    private String s3Key;
    private int thumbnailWidth;
    private int thumbnailHeight;

    //-- SqsConsumer
    @Override
    public void consume(final String body) throws IOException {
        final long id = Long.parseLong(body.toString());
        final Image img = imageRepository.findOne(id);
        consume(img);
    }

    private void consume(final Image img) throws IOException {
        LOGGER.info("Resizing {} to {}x{}", img.getUrl(), thumbnailWidth, thumbnailHeight);

        // Load image
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        loadImageContent(img, out);

        // Resize
        final String s3Key = img.getS3Key();
        final String extension = Files.getFileExtension(s3Key);
        final ByteArrayOutputStream rout = new ByteArrayOutputStream();
        imageProcessorService.resize(thumbnailWidth, thumbnailHeight, new ByteArrayInputStream(out.toByteArray()), rout, extension);

        // Store
        final String key = s3Key.substring(0, s3Key.length() - extension.length()-1) + "-thumb." + extension;
        final byte[] bytes = rout.toByteArray();
        final ObjectMetadata meta = createObjectMetadata(img, bytes.length);
        s3.putObject(s3Bucket, key, new ByteArrayInputStream(bytes), meta);

        // Create new image
        createThumbnail(img, key, bytes.length);
    }

    private void loadImageContent(final Image img, final OutputStream out) throws IOException {
        try (final S3Object obj = s3.getObject(s3Bucket, img.getS3Key())) {
            final InputStream in = obj.getObjectContent();
            IOUtils.copy(in, out);
        }
    }

    private ObjectMetadata createObjectMetadata(final Image image, final int len) {
        final ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(len);
        meta.setContentType(image.getContentType());

        return meta;
    }

    private void createThumbnail(final Image img, final String s3Key, final long len){
        final Image ximg = new Image();
        ximg.setHeight(thumbnailHeight);
        ximg.setLink(img.getLink());
        ximg.setS3Key(s3Key);
        ximg.setType(Image.TYPE_THUMBNAIL);
        ximg.setUrl(img.getUrl());
        ximg.setWidth(thumbnailWidth);
        ximg.setContentLength(len);
        ximg.setContentType(img.getContentType());
        imageRepository.save(ximg);
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

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(final int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    public void setThumbnailHeight(final int thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }
}
