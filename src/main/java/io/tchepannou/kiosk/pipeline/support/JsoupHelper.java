package io.tchepannou.kiosk.pipeline.support;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Collection;

public class JsoupHelper {
    private JsoupHelper(){
    }

    public static void collect (Element root, Collection<Element> result, Predicate<Element> predicate){
        if (predicate.test(root)){
            result.add(root);
        }

        Elements children = root.children();
        for (Element child : children){
            collect(child, result, predicate);
        }
    }

    public interface Predicate<T>{
        boolean test(T obj);
    }}
