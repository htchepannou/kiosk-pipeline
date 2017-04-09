package io.tchepannou.kiosk.pipeline.step.metadata;

import org.jsoup.nodes.Document;

import java.util.List;
import java.util.stream.Collectors;

public class HtmlTagExtractor {
    public List<String> extract(final Document doc){
        return extractMeta("article:tag", doc);
    }

    private List<String> extractMeta(final String name, final Document doc){
        return doc.select("meta[property=" + name + "]")
                .stream()
                .map(e -> e.attr("content"))
                .collect(Collectors.toList());

    }
}
