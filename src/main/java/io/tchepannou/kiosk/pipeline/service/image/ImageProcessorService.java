package io.tchepannou.kiosk.pipeline.service.image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ImageProcessorService {
    public void resize(
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
}
