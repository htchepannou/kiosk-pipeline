package io.tchepannou.kiosk.pipeline.service.extractor;

import io.tchepannou.kiosk.pipeline.support.JsoupHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Extract main content
 */
public class ContentFilter implements Filter<String>{
    //-- Attributes
    private static final List<String> TAG_INLINE = Arrays.asList("br", "a", "span");
    private static final List<String> TAG_FORMATTING = Arrays.asList("i", "b", "font", "em", "small", "mark", "del", "ins", "sub", "sup", "strong");
    public static final String CONTENT_ID = "main-content";

    private final int blocMinLen;

    public ContentFilter(final int blocMinLen){
        this.blocMinLen = blocMinLen;
    }

    public String filter(final String html) {

        final List<Element> parts = partition(html);
        final Map<Element, Integer> histogram = histogram(parts);
        final Element top = argmax(histogram);
        if (top != null) {
            final Element result = merge(top, parts);
            result.attr("id", CONTENT_ID);
            return result.html();
        } else {
            return "";
        }
    }

    //-- Private
    private List<Element> partition(final String html) {
        final JsoupHelper.Predicate<Element> predicate = new JsoupHelper.Predicate<Element>() {
            @Override
            public boolean test(final Element f) {
                /* exclude all the links to avoid picking menu blocs */
                final Element elt = f.clone();
                final List<Element> links = new ArrayList<>(elt.select("a"));
                for (final Element link : links) {
                    if (!link.equals(elt)) {
                        link.remove();
                    }
                }

                /* test */
                final String tagName = elt.tagName();
                return !"a".equals(tagName) && isLeaf(elt) && elt.text().length() > blocMinLen && !TAG_FORMATTING.contains(tagName);
            }
        };

        final Element body = Jsoup.parse(html).body();
        final List<Element> blocs = new ArrayList<>();
        JsoupHelper.collect(body, blocs, predicate);

        return blocs;
    }

    private boolean isLeaf(final Element elt) {
        final Element xelt = elt.clone();
        for (final Element child : xelt.children()) {
            final String tagName = child.tagName();
            if (TAG_INLINE.contains(tagName) || TAG_FORMATTING.contains(tagName)) {
                child.remove();
            }
        }
        return xelt.children().isEmpty();
    }

    private Map<Element, Integer> histogram(final List<Element> parts) {
        final Map<Element, Integer> result = new LinkedHashMap<>();
        for (final Element part : parts) {
            final StringTokenizer tokenizer = new StringTokenizer(part.text(), ".!?", false);
            int value = 0;
            for (; tokenizer.hasMoreTokens(); tokenizer.nextToken()) {
                value++;
            }
            result.put(part, value);
        }

        return result;
    }

    private Element argmax(final Map<Element, Integer> parts) {
        int maxValue = -1;
        Element maxElement = null;

        final Map<Element, Integer> result = new LinkedHashMap<>();
        for (final Element part : parts.keySet()) {
            final int value = parts.get(part);

            final Element parent = part.parent();
            final int pvalue = value + (result.containsKey(parent) ? result.get(parent) : 0);
            result.put(parent, pvalue);

            if (pvalue > maxValue) {
                maxValue = pvalue;
                maxElement = parent;
            }
        }

        return maxElement;
    }

    private Element merge(final Element parent, final Collection<Element> parts) {
        final List<Element> items = new ArrayList<>();
        for (final Element child : parent.children()) {
            if (!parts.contains(child)) {
                items.add(child);
            }
        }

        for (final Element item : items) {
            item.remove();
        }

        return parent;
    }
}
