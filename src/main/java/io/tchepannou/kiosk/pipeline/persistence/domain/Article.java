package io.tchepannou.kiosk.pipeline.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.util.Date;

@Entity
@Deprecated
public class Article {
    public static final int STATUS_CREATED = 0x00;
    public static final int STATUS_INVALID = 0x01;
    public static final int STATUS_DUPLICATE = 0x02;
    public static final int STATUS_VALID = 0x10;
    public static final int STATUS_PUBLISHED = 0x20;
    public static final int SUMMARY_MAX_LEN = 100;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne
    @JoinColumn(name = "link_fk")
    private Link link;

    @Column(name = "s3_key", columnDefinition = "TEXT")
    private String s3Key;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String displayTitle;

    @Column(columnDefinition = "TEXT")
    private String summary;

    private int status;

    @Column(name = "content_length")
    private int contentLength;

    @Column(name = "published_date")
    private Date publishedDate;

    @Column(name = "invalid_reason", length = 64)
    private String invalidReason;

    @Column(name = "duplicate_id")
    private Long duplicateId;

    public static String normalizeSummary(final String summary) {
        if (summary == null) {
            return null;
        }

        return summary.length() > SUMMARY_MAX_LEN ? summary.substring(0, SUMMARY_MAX_LEN) + "..." : summary;
    }

    //-- Getter/Setter
    @Transient
    public boolean isDuplicate () {
        return status == STATUS_DUPLICATE;
    }

    @Transient
    public boolean isPublished () {
        return status == STATUS_PUBLISHED;
    }

    @Transient
    public boolean isValid () {
        return status == STATUS_VALID;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(final Link link) {
        this.link = link;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(final String s3Key) {
        this.s3Key = s3Key;
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

    public Date getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(final Date publishedDate) {
        this.publishedDate = publishedDate;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(final int contentLength) {
        this.contentLength = contentLength;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(final String invalidReason) {
        this.invalidReason = invalidReason;
    }

    public Long getDuplicateId() {
        return duplicateId;
    }

    public void setDuplicateId(final Long duplicateId) {
        this.duplicateId = duplicateId;
    }

}
