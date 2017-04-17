package io.tchepannou.kiosk.pipeline.step;

import io.tchepannou.kiosk.persistence.domain.Feed;
import io.tchepannou.kiosk.persistence.domain.Link;
import io.tchepannou.kiosk.persistence.domain.LinkStatusEnum;
import io.tchepannou.kiosk.persistence.domain.LinkTypeEnum;

public abstract class AbstractImageConsumer extends AbstractLinkConsumer {

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
            img.setStatus(LinkStatusEnum.valid);

            linkRepository.save(img);
        }

        return img;
    }
}
