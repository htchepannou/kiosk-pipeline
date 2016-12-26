package io.tchepannou.kiosk.pipeline.persistence.repository;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends CrudRepository<Article, Long> {
}
