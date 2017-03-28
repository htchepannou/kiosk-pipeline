package io.tchepannou.kiosk.pipeline.consumer;

import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.step.validation.Validation;
import io.tchepannou.kiosk.pipeline.step.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.transaction.Transactional;
import java.io.IOException;

@Transactional
@ConfigurationProperties("kiosk.pipeline.ArticleValidationConsumer")
@Deprecated
public class ArticleValidationConsumer implements SqsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArticleValidationConsumer.class);

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private Validator articleValidator;

    private String inputQueue;

    //-- SqsConsumer
    @Override
    public void consume(final String body) throws IOException {
        final long id = Long.parseLong(body);

        final Article article = articleRepository.findOne(id);
        final Link link = article.getLink();
        LOGGER.info("Validating {}", link.getUrl());
        final Validation validation = articleValidator.validate(article);

        if (validation.isSuccess()) {
            LOGGER.info("{} is valid", link.getUrl());
            article.setStatus(Article.STATUS_VALID);
        } else {
            LOGGER.info("{} is invalid. reason=", link.getUrl(), validation.getReason());
            article.setStatus(Article.STATUS_INVALID);
            article.setInvalidReason(validation.getReason());
        }
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
