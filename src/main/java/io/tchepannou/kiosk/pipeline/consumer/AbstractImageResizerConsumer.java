package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.io.Files;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsSnsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Image;
import io.tchepannou.kiosk.pipeline.persistence.repository.ImageRepository;
import io.tchepannou.kiosk.pipeline.service.image.ImageProcessorService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class AbstractImageResizerConsumer extends SqsSnsConsumer {
    @Autowired
    AmazonS3 s3;

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ImageProcessorService imageProcessorService;

    private String inputQueue;
    private String s3Bucket;
    private String s3Key;

    //-- Abstract
    public abstract int getResizeWith();

    public abstract int getResizeHeight();

    public abstract int getImageType();

    protected abstract Logger getLogger ();

    //-- SqsConsumer
    @Override
    public void consumeMessage(final String body) throws IOException {
        final long id = Long.parseLong(body.toString());
        final Image img = imageRepository.findOne(id);
        consume(img);
    }

    private void consume(final Image img) throws IOException {
        final int resizeWidth = getResizeWith();
        final int resizeHeight = getResizeHeight();
        if (!shouldResize(img, resizeWidth, resizeHeight)) {
            return;
        }

        // Load image
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        loadImageContent(img, out);

        // Resize
        getLogger().info("Resizing {} to {}x{}", img.getUrl(), resizeWidth, resizeHeight);
        final String s3Key = img.getS3Key();
        final String extension = Files.getFileExtension(s3Key);
        final ByteArrayOutputStream rout = new ByteArrayOutputStream();
        imageProcessorService.resize(resizeWidth, resizeHeight, new ByteArrayInputStream(out.toByteArray()), rout, extension);
        final byte[] bytes = rout.toByteArray();
        if (bytes.length == 0) {
            getLogger().warn("Cannot resize. Invalid image {}", img.getUrl());
            return;
        }

        // Store
        final String key = s3Key.substring(0, s3Key.length() - extension.length() - 1) + "-" + getImageType() + "." + extension;
        final ObjectMetadata meta = createObjectMetadata(img, bytes.length);
        s3.putObject(s3Bucket, key, new ByteArrayInputStream(bytes), meta);

        // Create new image
        createImage(img, key, bytes.length);
    }

    private boolean shouldResize(final Image img, final int resizeWidth, final int resizeHeight) {
        final int imageSize = img.getWidth() * img.getHeight();
        final int size = resizeWidth * resizeHeight;

        return imageSize >= size;
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

    private void createImage(final Image img, final String s3Key, final long len) {
        final Image ximg = new Image();
        ximg.setHeight(getResizeHeight());
        ximg.setLink(img.getLink());
        ximg.setS3Key(s3Key);
        ximg.setType(getImageType());
        ximg.setUrl(img.getUrl());
        ximg.setWidth(getResizeWith());
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
}
