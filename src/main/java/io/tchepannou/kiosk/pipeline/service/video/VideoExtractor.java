package io.tchepannou.kiosk.pipeline.service.video;

import io.tchepannou.kiosk.pipeline.service.content.SanitizeFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoExtractor {
    private List<VideoService> services;

    public VideoExtractor(final List<VideoService> services){
        this.services = services;
    }

    public List<String> extract(final String html){
        final List<String> urls = new ArrayList<>();

        final Document doc = Jsoup.parse(sanitize(html));
        final Elements elts = doc.select("iframe");
        for (Element elt : elts){
            final String src = elt.attr("src");
            final String url = getEmbedUrl(src);
            if (url != null && !urls.contains(url)){
                urls.add(url);
            }
        }
        return urls;
    }

    private String sanitize (final String html){
        final List<String> tags = new ArrayList<>(Arrays.asList(SanitizeFilter.TAG_WHITELIST));
        tags.add("iframe");

        SanitizeFilter filter = new SanitizeFilter(tags);
        return filter.filter(html);
    }
    private String getEmbedUrl(final String url){
        for (VideoService service : services){
            final String id = service.getVideoId(url);
            if (id != null){
                return service.getEmbedUrl(id);
            }
        }
        return null;
    }
}
