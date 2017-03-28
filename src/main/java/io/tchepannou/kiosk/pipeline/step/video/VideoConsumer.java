package io.tchepannou.kiosk.pipeline.step.video;

import io.tchepannou.kiosk.core.service.MessageQueue;
import io.tchepannou.kiosk.pipeline.persistence.domain.AssetTypeEnum;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkStatusEnum;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTypeEnum;
import io.tchepannou.kiosk.pipeline.step.AbstractLinkConsumer;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Transactional
public class VideoConsumer extends AbstractLinkConsumer {
    @Autowired
    @Qualifier("PublishMessageQueue")
    MessageQueue queue;

    private final List<VideoProvider> providers;

    public VideoConsumer(final List<VideoProvider> providers) {
        this.providers = providers;
    }

    @Override
    protected void consume(final Link link) throws IOException {
        final Document doc = getRawDocument(link);
        final List<String> urls = extract(doc);
        for (final String url : urls) {
            final Link video = createVideo(link.getFeed(), url);
            if (video != null) {
                createAsset(link, video, AssetTypeEnum.video);
            }
            if (!video.isPublished()) {
                push(video, queue);
            }
        }
    }

    private Link createVideo(
            final Feed feed,
            final String url
    ) {
        final String urlHash = Link.hash(url);
        Link video = linkRepository.findByUrlHash(urlHash);
        if (video == null) {
            video = new Link();
            video.setFeed(feed);
            video.setUrl(url);
            video.setUrlHash(urlHash);
            video.setType(LinkTypeEnum.video);
            video.setStatus(LinkStatusEnum.valid);

            linkRepository.save(video);
        }

        return video;
    }

    private List<String> extract(final Document doc) {
        final List<String> urls = new ArrayList<>();

        final Elements elts = doc.select("iframe");
        for (final Element elt : elts) {
            final String src = elt.attr("src");
            final String url = getEmbedUrl(src);
            if (url != null && !urls.contains(url)) {
                urls.add(url);
            }
        }
        return urls;
    }

    private String getEmbedUrl(final String url) {
        for (final VideoProvider provider : providers) {
            final String embedUrl = provider.getEmbedUrl(url);
            if (embedUrl != null) {
                return embedUrl;
            }
        }
        return null;
    }

}
