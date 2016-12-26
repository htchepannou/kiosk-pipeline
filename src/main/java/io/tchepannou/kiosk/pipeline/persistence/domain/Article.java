package io.tchepannou.kiosk.pipeline.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.util.Date;

@Entity
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToOne
    @JoinColumn(name="link_fk")
    private Link link;

    @Column(name = "s3_key", columnDefinition = "TEXT")
    private String s3Key;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private int status;

    @Column(name = "published_date")
    private Date publishedDate;

    //-- Getter/Setter
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

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
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
}
