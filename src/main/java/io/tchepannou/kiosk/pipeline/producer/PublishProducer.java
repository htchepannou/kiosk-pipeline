package io.tchepannou.kiosk.pipeline.producer;

import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public class PublishProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishProducer.class);

    @Autowired
    ArticleRepository articleRepository;

    public void produce (){
        final List<Article> articles = articleRepository.findByStatus(Article.STATUS_VALID);
        LOGGER.info("{} article(s) to publish", articles.size());

        for (Article article : articles){
            LOGGER.info("Publishing {}", article.getLink().getUrl());
            article.setStatus(Article.STATUS_PUBLISHED);
        }
        articleRepository.save(articles);
    }
}
