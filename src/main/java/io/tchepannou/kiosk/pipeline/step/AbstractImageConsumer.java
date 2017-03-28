package io.tchepannou.kiosk.pipeline.step;

import io.tchepannou.kiosk.pipeline.persistence.domain.Asset;
import io.tchepannou.kiosk.pipeline.persistence.domain.AssetTypeEnum;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTypeEnum;
import io.tchepannou.kiosk.pipeline.persistence.repository.AssetRepository;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractImageConsumer extends AbstractLinkConsumer {
    @Autowired
    protected AssetRepository assetRepository;

    protected Link createImage(
            final Feed feed,
            final String url,
            final String key,
            final String contentType,
            final int contentLength,
            final int width,
            final int height
    ) {
        final String urlHash = Link.hash(url);
        Link img = linkRepository.findByUrlHash(urlHash);
        if (img == null) {
            img = new Link();
            img.setFeed(feed);
            img.setUrl(url);
            img.setUrlHash(urlHash);
            img.setS3Key(key);
            img.setContentLength(contentLength);
            img.setContentType(contentType);
            img.setWidth(width);
            img.setHeight(height);
            img.setType(LinkTypeEnum.image);

            linkRepository.save(img);
        }

        return img;
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
