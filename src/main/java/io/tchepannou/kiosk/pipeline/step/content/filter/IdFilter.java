package io.tchepannou.kiosk.pipeline.step.content.filter;

import io.tchepannou.kiosk.pipeline.step.content.Filter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class IdFilter implements Filter<String> {
    @Override
    public String filter(final String html) {
        final Document doc = Jsoup.parse(html);
        visit(doc.body(), 1);

        return doc.html();
    }

    private int visit (final Element elt, final int index){
        int xindex = index;
        if (elt.isBlock() && !elt.hasAttr("id")){
            elt.attr("id", String.format("item_%s", xindex++));
        }

        for (Element child : elt.children()){
            xindex = visit(child, xindex);
        }
        return xindex;
    }
}
