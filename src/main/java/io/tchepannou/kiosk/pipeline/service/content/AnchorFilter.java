package io.tchepannou.kiosk.pipeline.service.content;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Cleanup the anchor to open them in a new window via the function <code>navigate(url)</code> that will be implemented by the UI
 */
public class AnchorFilter implements Filter<String> {
    //-- TextFilter overrides
    @Override
    public String filter(final String html) {
        final Document doc = Jsoup.parse(html);
        final Elements elts = doc.select("a");
        for (final Element elt : elts) {
            elt.removeAttr("onclick");

            final String href = elt.attr("href").trim();
            if (isValid(href)) {
                elt.attr("onclick", "navigate('" + href + "')");
                elt.addClass("kiosk-link");
            }
            elt.attr("href", "#");
        }

        return doc.html();
    }

    private boolean isValid(final String url) {
        return (url.startsWith("http://") || url.startsWith("https://"))
                && !url.contains("#");
    }
}
