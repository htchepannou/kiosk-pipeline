package io.tchepannou.kiosk.pipeline.service.video;

import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.domain.LinkTypeEnum;
import io.tchepannou.kiosk.pipeline.service.content.SanitizeFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoExtractor {
    private final List<VideoService> services;

    public VideoExtractor(final List<VideoService> services) {
        this.services = services;
    }

    //-- Public
    public List<Link> extractLinks(final String html, final Feed feed) {
        final List<Link> links = new ArrayList<>();

        final Document doc = Jsoup.parse(sanitize(html));
        final Elements elts = doc.select("iframe");
        for (final Element elt : elts) {
            final String src = elt.attr("src");
            final Link link = toLink(src, feed);
            if (link != null) {
                links.add(link);
            }
        }
        return links;
    }

    public List<String> extract(final String html) {
        final List<String> urls = new ArrayList<>();

        final Document doc = Jsoup.parse(sanitize(html));
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

    //-- Private
    private String sanitize(final String html) {
        final List<String> tags = new ArrayList<>(Arrays.asList(SanitizeFilter.TAG_WHITELIST));
        tags.add("iframe");

        final SanitizeFilter filter = new SanitizeFilter(tags);
        return filter.filter(html);
    }

    private Link toLink(final String url, final Feed feed) {
        for (final VideoService service : services) {
            final String id = service.getVideoId(url);
            if (id != null) {
                final Link link = new Link();
                link.setUrl(url);
                link.setType(LinkTypeEnum.video);
                link.setProvider(service.getName());
                link.setFeed(feed);
                link.setEmbedUrl(service.getEmbedUrl(id));
                return link;
            }
        }
        return null;

    }

    private String getEmbedUrl(final String url) {
        for (final VideoService service : services) {
            final String id = service.getVideoId(url);
            if (id != null) {
                return service.getEmbedUrl(id);
            }
        }
        return null;
    }
}
