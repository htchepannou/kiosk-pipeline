package io.tchepannou.kiosk.pipeline.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

@Entity
public class Feed {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(length = 64)
    private String name;

    private String url;

    @Column(length = 64)
    private String path;

    @Column(name="logo_url")
    private String logoUrl;

    @Column(name="display_title_regex", length = 64)
    private String displayTitleRegex;

    @Column(name="onboard_date")
    private Date onboardDate;

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

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(final String logoUrl) {
        this.logoUrl = logoUrl;
    }

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
        if (this.url != null && this.url.endsWith("/")){
            this.url = this.url.substring(0, this.url.length()-1);
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public String getDisplayTitleRegex() {
        return displayTitleRegex;
    }

    public void setDisplayTitleRegex(final String displayTitleRegex) {
        this.displayTitleRegex = displayTitleRegex;
    }

    public Date getOnboardDate() {
        return onboardDate;
    }

    public void setOnboardDate(final Date onboardDate) {
        this.onboardDate = onboardDate;
    }
}
