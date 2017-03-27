package io.tchepannou.kiosk.pipeline.persistence.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;


    @ManyToOne
    @JoinColumn(name = "link_fk")
    private Link link;

    @ManyToOne
    @JoinColumn(name = "target_fk")
    private Link target;

    @Column(length = 20)
    private String type;

    public Asset() {
    }

    public Asset(final Link link, final Link target, final String type) {
        this.link = link;
        this.target = target;
        this.type = type;
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

    public Link getTarget() {
        return target;
    }

    public void setTarget(final Link target) {
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }
}
