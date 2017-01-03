package io.tchepannou.kiosk.pipeline.service.image;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import static io.tchepannou.kiosk.pipeline.support.JsoupHelper.selectMeta;

public class ImageExtractor {
    public String extract(final String html) {
        final Document doc = Jsoup.parse(html);
        String url = selectMeta(doc, "meta[property=og:image]");
        if (url == null){
            url = selectMeta(doc, "meta[property=twitter:image]");
        }
        if (url == null){
            url = selectMeta(doc, "meta[property=shareaholic:image]");
        }

        return url;
    }
}
