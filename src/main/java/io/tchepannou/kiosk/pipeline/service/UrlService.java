package io.tchepannou.kiosk.pipeline.service;

import com.google.common.base.Strings;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UrlService {
    @Autowired
    HttpService http;

    private List<String> blacklist = new ArrayList<>();

    public Collection<String> extractUrls(Feed feed) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final String url = feed.getUrl();
        http.get(url, out);

        final String html = out.toString("utf-8");
        final Document doc = Jsoup.parse(html);
        doc.setBaseUri(url);
        return doc.select("a")
                .stream()
                .map(e -> normalize(e.attr("abs:href")))
                .filter(href -> feed.urlMatches(href))
                .collect(Collectors.toSet());
    }

    public String normalize(final String url) {
        if (url == null){
            return "";
        }

        String result = url.trim();
        if (result.endsWith("/")){
            result = result.substring(0, result.length() - 1);
        }

        return result.trim().toLowerCase();
    }

    public boolean isBlacklisted(String urlToVerify) {
        if (Strings.isNullOrEmpty(urlToVerify)){
            return true;
        }

        for (final String url : blacklist){
            if (matches(url, urlToVerify)){
                return true;
            }
        }
        return false;
    }

    private boolean matches(final String url, final String urlToVerify) {
        final String regex = (url.toLowerCase()).replace("*", ".*");
        return urlToVerify.matches(regex);
    }

    public List<String> getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(final List<String> blacklist) {
        this.blacklist = blacklist;
    }
}
