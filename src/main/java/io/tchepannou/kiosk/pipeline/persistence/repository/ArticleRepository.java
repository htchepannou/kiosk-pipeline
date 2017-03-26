package io.tchepannou.kiosk.pipeline.persistence.repository;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ArticleRepository extends CrudRepository<Article, Long> {
    Article findByLink(Link link);
    List<Article> findByStatus(int status);
    List<Article> findByStatusInAndPublishedDateBetween(List<Integer> status, Date publishedDateStart, Date publishedDateEnd);
}
