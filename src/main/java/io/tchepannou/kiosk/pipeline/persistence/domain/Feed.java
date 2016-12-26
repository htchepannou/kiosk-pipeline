package io.tchepannou.kiosk.pipeline.persistence.domain;

import java.io.IOException;
import java.net.URL;

public class Feed {
    private String name;
    private String url;
    private String path;

    //-- Public
    public boolean urlMatches(String uri) {
        uri = uri.toLowerCase();
        final boolean matches = uri.equals(url) || uri.toLowerCase().startsWith(url + "/");

        if (matches && path != null) {
            final String regex = ("\\Q" + path + "\\E").replace("*", "\\E.*\\Q");
            try {
                final String file = new URL(uri).getPath();
                return file.matches(regex);
            } catch (final IOException e) {
                return false;
            }
        }

        return matches;
    }

    //-- Getter/Setter
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url != null ? url.toLowerCase() : null;
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }
}
