package io.tchepannou.kiosk.pipeline.consumer;

import io.tchepannou.kiosk.pipeline.persistence.domain.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.transaction.Transactional;

@ConfigurationProperties("kiosk.pipeline.ImageThumbnailConsumer")
@Transactional
public class ImageThumbnailConsumer extends AbstractImageResizerConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageThumbnailConsumer.class);

    private int thumbnailWidth;
    private int thumbnailHeight;

    @Override
    public int getResizeWith() {
        return thumbnailWidth;
    }

    @Override
    public int getResizeHeight() {
        return thumbnailHeight;
    }

    @Override
    public int getImageType() {
        return Image.TYPE_THUMBNAIL;
    }


    //-- Getter/Setter
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
