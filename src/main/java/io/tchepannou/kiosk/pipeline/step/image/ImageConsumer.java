package io.tchepannou.kiosk.pipeline.step.image;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.persistence.domain.AssetTypeEnum;
import io.tchepannou.kiosk.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.step.AbstractImageConsumer;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import static io.tchepannou.kiosk.pipeline.support.JsoupHelper.selectMeta;

@Transactional
public class ImageConsumer extends AbstractImageConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageConsumer.class);

    @Autowired
    @Qualifier("ThumbnailMessageQueue")
    MessageQueue queue;

    @Autowired
    HttpService http;

    String rawFolder;
    String imageFolder;

    @Override
    protected void consume(final Link link) throws IOException {
        final Document doc = getRawDocument(link);
        final String url = extract(doc);
        if (Strings.isNullOrEmpty(url)) {
            return;
        }

        final String urlHash = Link.hash(url);
        Link img = linkRepository.findByUrlHash(urlHash);
        if (img == null) {
            img = download(url, link);
        }

        if (img != null) {
            createAsset(link, img, AssetTypeEnum.original);
            push(img, queue);
        }
    }

    private Link download(final String url, final Link link) throws IOException {

        // Download
        LOGGER.info("Downloading {}", url);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final String contentType = http.get(url, out);
        if (contentType == null || !contentType.startsWith("image/")) {
            LOGGER.error("{} has invalid content-type: {}", url, contentType);
            return null;
        }

        // Store content
        final String key = imageKey(url, link);
        final byte[] bytes = out.toByteArray();
        repository.write(key, new ByteArrayInputStream(bytes));

        // Store image
        final BufferedImage bimg = ImageIO.read(new ByteArrayInputStream(bytes));
        if (bimg == null) {
            LOGGER.error("{} is corrupted. Cannot load the image!", url);
            return null;
        }
        return createImage(
                link.getFeed(),
                url,
                key,
                contentType,
                bytes.length,
                bimg.getWidth(),
                bimg.getHeight()
        );
    }

    private String extract(final Document doc) {
        String url = selectMeta(doc, "meta[property=og:image]");
        if (url == null) {
            url = selectMeta(doc, "meta[property=twitter:image]");
        }
        if (url == null) {
            url = selectMeta(doc, "meta[property=shareaholic:image]");
        }

        return url;
    }

    private String imageKey(final String url, final Link link) throws IOException {
        final String key = link.getS3Key();
        final String filename = normalizeFilename(new URL(url).getFile());
        final String extension = Files.getFileExtension(filename);
        return imageFolder + key.substring(rawFolder.length(), key.length() - 4) + extension;
    }

    private String normalizeFilename(final String filename) {
        final int i = filename.indexOf('?');
        return (i > 0 ? filename.substring(0, i) : filename).toLowerCase();
    }

    public String getImageFolder() {
        return imageFolder;
    }

    public void setImageFolder(final String imageFolder) {
        this.imageFolder = imageFolder;
    }

    public String getRawFolder() {
        return rawFolder;
    }

    public void setRawFolder(final String rawFolder) {
        this.rawFolder = rawFolder;
    }
}
