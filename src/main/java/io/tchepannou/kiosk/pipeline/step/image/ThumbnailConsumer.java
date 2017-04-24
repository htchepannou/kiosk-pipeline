package io.tchepannou.kiosk.pipeline.step.image;

import com.google.common.io.Files;
import io.tchepannou.kiosk.persistence.domain.Asset;
import io.tchepannou.kiosk.persistence.domain.AssetTypeEnum;
import io.tchepannou.kiosk.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.step.AbstractImageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@Transactional
public class ThumbnailConsumer extends AbstractImageConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailConsumer.class);

    private int width;
    private int height;

    @Override
    protected void consume(final Link img) throws IOException {
        Link thumbnail;
        float resizeWidth = 0;
        float resizeHeight = 0;
        float ratio;

        if (img.getWidth() > width){
            ratio = width/img.getWidth();

            resizeWidth = width;
            resizeHeight = img.getHeight()*ratio;
        } else if (img.getHeight() > height){
            ratio = height/img.getHeight();

            resizeHeight = height;
            resizeWidth = img.getWidth() * ratio;
        }

        if (resizeHeight != 0 && resizeWidth != 0) {
            thumbnail = resize(img, (int) resizeWidth, (int) resizeHeight);
        } else {
            thumbnail = img;
        }

        if (thumbnail != null){
            // Link with articles
            final List<Asset> assets = assetRepository.findByTargetAndType(img, AssetTypeEnum.original);
            for (final Asset asset : assets) {
                createAsset(asset.getLink(), thumbnail, AssetTypeEnum.thumbnail);
            }
        }
    }


    //-- Private
    private Link resize (final Link img, final int resizeWidth, final int resizeHeight) throws IOException {
        // Load image
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        repository.read(img.getS3Key(), out);

        // Resize
        LOGGER.info("Resizing {} to {}x{}", img.getUrl(), resizeWidth, resizeHeight);
        final String s3Key = img.getS3Key();
        final String extension = Files.getFileExtension(s3Key);
        final ByteArrayOutputStream rout = new ByteArrayOutputStream();
        resize(resizeWidth, resizeHeight, new ByteArrayInputStream(out.toByteArray()), rout, extension);
        final byte[] bytes = rout.toByteArray();
        if (bytes.length == 0) {
            LOGGER.warn("Cannot resize. Invalid image {}", img.getUrl());
            return null;
        }

        // Store
        final String key = s3Key.substring(0, s3Key.length() - extension.length() - 1) + "-thumb." + extension;
        repository.write(key, new ByteArrayInputStream(bytes));

        // Create new image
        return createImage(
                img.getFeed(),
                key,
                key,
                img.getContentType(),
                bytes.length,
                resizeWidth,
                resizeHeight
        );
    }

    private void resize(
            final int width,
            final int height,
            final InputStream in,
            final OutputStream out,
            final String extension
    ) throws IOException {
        final BufferedImage inputImage = ImageIO.read(in);
        if (inputImage == null){
            return;
        }

        final BufferedImage outputImage = new BufferedImage(width, height, inputImage.getType());

        final Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, width, height, null);
        g2d.dispose();

        // extracts extension of output file
        ImageIO.write(outputImage, extension, out);
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
