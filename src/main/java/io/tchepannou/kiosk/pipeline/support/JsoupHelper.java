package io.tchepannou.kiosk.pipeline.support;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collection;

public class JsoupHelper {
    private JsoupHelper() {
    }

    public static void collect(final Element root, final Collection<Element> result, final Predicate<Element> predicate) {
        if (predicate.test(root)) {
            result.add(root);
        }

        final Elements children = root.children();
        for (final Element child : children) {
            collect(child, result, predicate);
        }
    }

    public static String selectMeta(final Document doc, final String cssSelector) {
        final Elements elts = doc.select(cssSelector);
        return elts.isEmpty() ? null : elts.attr("content");
    }

    public static String select(final Document doc, final String cssSelector) {
        final Elements elts = doc.select(cssSelector);
        return elts.isEmpty() ? null : elts.text();
    }

    public interface Predicate<T> {
        boolean test(T obj);
    }
}
