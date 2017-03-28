package io.tchepannou.kiosk.pipeline.consumer;

import io.tchepannou.kiosk.pipeline.persistence.domain.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.transaction.Transactional;

@ConfigurationProperties("kiosk.pipeline.ImageThumbnailConsumer")
@Transactional
@Deprecated
public class ImageThumbnailConsumer extends AbstractImageResizerConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageThumbnailConsumer.class);

    private int width;
    private int height;

    @Override
    public int getResizeWith() {
        return width;
    }

    @Override
    public int getResizeHeight() {
        return height;
    }

    @Override
    public int getImageType() {
        return Image.TYPE_THUMBNAIL;
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    //-- Getter/Setter
    public int getWidth() {
        return width;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(final int height) {
        this.height = height;
    }
}
