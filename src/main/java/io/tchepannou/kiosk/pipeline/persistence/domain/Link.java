package io.tchepannou.kiosk.pipeline.persistence.domain;

import org.apache.commons.codec.digest.DigestUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Link {
    public static final int STATUS_CREATED = 0x00;
    public static final int STATUS_INVALID = 0x01;
    public static final int STATUS_DUPLICATE = 0x02;
    public static final int STATUS_VALID = 0x10;
    public static final int STATUS_PUBLISHED = 0x20;
    public static final int SUMMARY_MAX_LEN = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "feed_fk")
    private Feed feed;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(name = "s3_key", columnDefinition = "TEXT")
    private String s3Key;

    @Column(name="url_hash", columnDefinition = "char(32)")
    private String urlHash;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String displayTitle;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private int status = STATUS_CREATED;

    @Column(length = 32)
    private String provider;

    @Column(length = 255)
    private String embedUrl;

    private LinkTypeEnum type = null;

    //-- Public
    public static String hash(final String url) {
        return DigestUtils.md5Hex(url);
    }

    //-- Getter/Setter
    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
        this.urlHash = url == null ? null : hash(url);
    }

    public String getUrlHash() {
        return urlHash;
    }

    public void setUrlHash(final String urlHash) {
        this.urlHash = urlHash;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(final String s3Key) {
        this.s3Key = s3Key;
    }

    public Feed getFeed() {
        return feed;
    }

    public void setFeed(final Feed feed) {
        this.feed = feed;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(final String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(final int status) {
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(final String provider) {
        this.provider = provider;
    }

    public String getEmbedUrl() {
        return embedUrl;
    }

    public void setEmbedUrl(final String embedUrl) {
        this.embedUrl = embedUrl;
    }

    public LinkTypeEnum getType() {
        return type;
    }

    public void setType(final LinkTypeEnum type) {
        this.type = type;
    }
}
