package io.tchepannou.kiosk.pipeline.step;

import io.tchepannou.kiosk.core.service.Consumer;
import io.tchepannou.kiosk.core.service.FileRepository;
import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.Asset;
import io.tchepannou.kiosk.pipeline.persistence.domain.AssetTypeEnum;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.AssetRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class AbstractLinkConsumer implements Consumer {
    @Autowired
    protected LinkRepository linkRepository;

    @Autowired
    protected AssetRepository assetRepository;

    @Autowired
    protected FileRepository repository;

    abstract protected void consume(Link link) throws IOException;

    @Override
    public void consume(final String message) throws IOException {
        final Link link = linkRepository.findOne(Long.parseLong(message.trim()));
        consume(link);
    }

    protected void push(final Link link, final MessageQueue queue) throws IOException {
        queue.push(String.valueOf(link.getId()));
    }

    protected String getContentHtml(final Link link) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        repository.read(link.getContentKey(), out);

        return IOUtils.toString(new ByteArrayInputStream(out.toByteArray()), "utf-8");
    }

    protected Document getContentDocument(final Link link) throws IOException {
        final String html = getContentHtml(link);
        return Jsoup.parse(html);
    }

    protected String getRawHtml(final Link link) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        repository.read(link.getS3Key(), out);

        return IOUtils.toString(new ByteArrayInputStream(out.toByteArray()), "utf-8");
    }

    protected Document getRawDocument(final Link link) throws IOException {
        final String html = getRawHtml(link);
        return Jsoup.parse(html);
    }

    protected Asset createAsset(
            final Link link,
            final Link img,
            final AssetTypeEnum assetType
    ) {
        Asset asset = assetRepository.findByLinkAndTargetAndType(link, img, assetType);
        if (asset == null) {
            asset = new Asset(link, img, assetType);
            assetRepository.save(asset);
        }
        return asset;
    }

}
