package io.tchepannou.kiosk.pipeline.service;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

public class UrlBlacklistService {
    private List<String> urls = new ArrayList<>();

    public boolean contains(String urlToVerify) {
        if (Strings.isNullOrEmpty(urlToVerify)){
            return true;
        }

        urlToVerify = urlToVerify.toLowerCase();
        if (urlToVerify.endsWith("/")){
            urlToVerify = urlToVerify.substring(0, urlToVerify.length()-1);
        }

        for (String url : urls){
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

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(final List<String> urls) {
        this.urls = urls;
    }
}
