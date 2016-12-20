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

    private String url;

    @Column(columnDefinition = "char(32)")
    private String keyhash;

    //-- Public
    public static String generateKeyHash(final String url){
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

    public String getKeyhash() {
        return keyhash;
    }

    public void setKeyhash(final String keyhash) {
        this.keyhash = keyhash;
    }
}
