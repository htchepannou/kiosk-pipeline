package io.tchepannou.kiosk.pipeline.step.image;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Asset;
import io.tchepannou.kiosk.pipeline.persistence.domain.AssetTypeEnum;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTypeEnum;
import io.tchepannou.kiosk.pipeline.persistence.repository.AssetRepository;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.step.LinkConsumer;
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
public class ImageConsumer extends LinkConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageConsumer.class);

    @Autowired
    @Qualifier("ThumbnailMessageQueue")
    MessageQueue queue;

    @Autowired
    AssetRepository assetRepository;

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
        join(link, img);

        push(img, queue);
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
        final Link img = new Link();
        img.setFeed(link.getFeed());
        img.setUrl(url);
        img.setUrlHash(Link.hash(url));
        img.setS3Key(key);
        img.setContentLength(bytes.length);
        img.setContentType(contentType);
        img.setWidth(bimg.getWidth());
        img.setHeight(bimg.getHeight());
        img.setType(LinkTypeEnum.image.name());
        linkRepository.save(img);
        return img;
    }

    private void join(final Link link, final Link img){
        final String type = AssetTypeEnum.original.name();
        Asset asset = assetRepository.findByLinkAndTargetAndType(link, img, type);
        if (asset == null){
            asset = new Asset(link, img, type);
            assetRepository.save(asset);
        }
    }

    private String extract(final Document doc) {
        String url = selectMeta(doc, "meta[property=og:image]");
        if (url == null){
            url = selectMeta(doc, "meta[property=twitter:image]");
        }
        if (url == null){
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
