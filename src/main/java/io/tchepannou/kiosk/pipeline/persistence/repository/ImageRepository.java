package io.tchepannou.kiosk.pipeline.persistence.repository;

import io.tchepannou.kiosk.pipeline.persistence.domain.Image;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends CrudRepository<Image, Long> {
    List<Image> findByLinkAndTypeAndUrl(Link link, int type, String url);
}
