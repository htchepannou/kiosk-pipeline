package io.tchepannou.kiosk.pipeline.persistence.domain;

import org.apache.commons.codec.digest.DigestUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.Date;

@Entity
public class Link {
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

    @Column(name = "content_key", columnDefinition = "TEXT")
    private String contentKey;

    @Column(name="url_hash", columnDefinition = "char(32)")
    private String urlHash;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String displayTitle;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "published_date")
    private Date publishedDate;

    @Column
    private LinkTypeEnum type;

    @Column(name="content_length")
    private int contentLength;

    @Column(length = 64)
    private String contentType;

    private LinkStatusEnum status = LinkStatusEnum.created;

    @Column(name="invalid_reason", length = 20)
    private String invalidReason;

    private int width;
    private int height;

    //-- Public
    public static String hash(final String url) {
        return DigestUtils.md5Hex(url);
    }

    //-- Getter/Setter
    @Transient
    public boolean isValid (){
        return LinkStatusEnum.valid.equals(status);
    }

    @Transient
    public boolean isPublished (){
        return LinkStatusEnum.published.equals(status);
    }

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

    public Date getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(final Date publishedDate) {
        this.publishedDate = publishedDate;
    }

    public LinkTypeEnum getType() {
        return type;
    }

    public void setType(final LinkTypeEnum type) {
        this.type = type;
    }

    public String getContentKey() {
        return contentKey;
    }

    public void setContentKey(final String contentKey) {
        this.contentKey = contentKey;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(final int contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(final String invalidReason) {
        this.invalidReason = invalidReason;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(final int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(final int height) {
        this.height = height;
    }

    public LinkStatusEnum getStatus() {
        return status;
    }

    public void setStatus(final LinkStatusEnum status) {
        this.status = status;
    }
}
