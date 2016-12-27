package io.tchepannou.kiosk.pipeline.service.image;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ImageProcessorServiceTest {
    @InjectMocks
    ImageProcessorService processor;

    @Test
    public void testResize() throws Exception {
        // Given
        final InputStream in = getClass().getResourceAsStream("/image/jordan.jpg");
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        // When
        processor.resize(100, 110, in, out, "jpg");

        // Then
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(out.toByteArray()));
        assertThat(img.getWidth()).isEqualTo(100);
        assertThat(img.getHeight()).isEqualTo(110);
    }
}
