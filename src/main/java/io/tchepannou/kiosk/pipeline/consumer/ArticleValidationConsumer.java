package io.tchepannou.kiosk.pipeline.consumer;

import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.service.validation.Validation;
import io.tchepannou.kiosk.pipeline.service.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.transaction.Transactional;
import java.io.IOException;

@Transactional
@ConfigurationProperties("kiosk.pipeline.ArticleValidationConsumer")
public class ArticleValidationConsumer implements SqsConsumer {
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
        final Validation validation = articleValidator.validate(article);

        if (validation.isSuccess()) {
            article.setStatus(Article.STATUS_VALID);
        } else {
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
