package io.tchepannou.kiosk.pipeline.consumer;

import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.io.IOException;

@Transactional
public class ArticlePublishConsumer implements SqsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleValidationConsumer.class);

    @Autowired
    private ArticleRepository articleRepository;

    private String inputQueue;

    //-- SqsConsumer
    @Override
    public void consume(final String body) throws IOException {
        final long id = Long.parseLong(body);

        final Article article = articleRepository.findOne(id);
        final Link link = article.getLink();
        LOGGER.info("Publishing {}", link.getUrl());

        article.setStatus(Article.STATUS_PUBLISHED);
        articleRepository.save(article);
    }

    //-- Getter/Setter
    public String getInputQueue() {
        return inputQueue;
    }

    public void setInputQueue(final String inputQueue) {
        this.inputQueue = inputQueue;
    }
}
