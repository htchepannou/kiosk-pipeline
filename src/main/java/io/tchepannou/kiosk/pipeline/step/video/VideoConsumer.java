package io.tchepannou.kiosk.pipeline.step.video;

import io.tchepannou.kiosk.pipeline.persistence.domain.AssetTypeEnum;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkStatusEnum;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTypeEnum;
import io.tchepannou.kiosk.pipeline.step.AbstractLinkConsumer;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Transactional
public class VideoConsumer extends AbstractLinkConsumer {
    private List<VideoProvider> providers = new ArrayList<>();

    @Override
    protected void consume(final Link link) throws IOException {
        final Document doc = getRawDocument(link);
        final List<String> urls = extract(doc);
        for (final String url : urls) {
            final Link video = createVideo(link.getFeed(), url);
            if (video != null) {
                createAsset(link, video, AssetTypeEnum.video);
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

            try {
                VideoInfo info = getInfo(url);
                if (info != null) {
                    video.setTitle(info.getTitle());
                    video.setSummary(info.getDescription());
                }
            } catch (IOException e){

            }

            linkRepository.save(video);
        }

        return video;
    }

    private VideoInfo getInfo(final String url) throws IOException {
        for (final VideoProvider provider : providers){
            final VideoInfo info = provider.getInfo(url);
            if (info != null){
                return info;
            }
        }
        return null;
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

    public List<VideoProvider> getProviders() {
        return providers;
    }

    public void setProviders(final List<VideoProvider> providers) {
        this.providers = providers;
    }
}