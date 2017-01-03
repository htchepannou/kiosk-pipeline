package io.tchepannou.kiosk.pipeline.service.video;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class VideoExtractor {
    private List<VideoService> services;

    public VideoExtractor(final List<VideoService> services){
        this.services = services;
    }

    public List<String> extract(final String html){
        final List<String> urls = new ArrayList<>();
        final Document doc = Jsoup.parse(html);
        final Elements elts = doc.select("iframe");
        for (Element elt : elts){
            final String src = elt.attr("src");
            final String url = getEmbedUrl(src);
            if (url != null){
                urls.add(url);
            }
        }
        return urls;
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
