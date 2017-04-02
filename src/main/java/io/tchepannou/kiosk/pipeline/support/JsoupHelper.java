package io.tchepannou.kiosk.pipeline.support;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collection;

public class JsoupHelper {
    private JsoupHelper() {
    }

    public static void visit(final Element root, final Visitor<Element> visitor){
        final Elements children = root.children();
        for (final Element child : children) {
            visit(child, visitor);
        }
        visitor.visit(root);
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
        return elts.isEmpty() ? null : elts.get(0).attr("content");
    }

    public static String select(final Document doc, final String cssSelector) {
        final Elements elts = doc.select(cssSelector);
        return elts.isEmpty() ? null : elts.get(0).text();
    }

    public interface Predicate<T> {
        boolean test(T obj);
    }

    public interface Visitor<T> {
        void visit(T obj);
    }
}
