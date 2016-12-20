package io.tchepannou.kiosk.pipeline.persistence.domain;

import org.apache.commons.codec.digest.DigestUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(name = "s3_key", columnDefinition = "TEXT")
    private String s3Key;

    @Column(name="url_hash", columnDefinition = "char(32)")
    private String urlHash;

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
}
