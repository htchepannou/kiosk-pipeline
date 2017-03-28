package io.tchepannou.kiosk.pipeline.producer;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Deprecated
public class PublishProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishProducer.class);

    @Autowired
    AmazonSQS sqs;

    @Autowired
    ArticleRepository articleRepository;

    private String outputQueue;

    public void produce (){
        final List<Article> articles = articleRepository.findByStatus(Article.STATUS_VALID);
        LOGGER.info("{} article(s) to publish", articles.size());

        for (Article article : articles){
            LOGGER.info("Sending <{}> to {}", article.getLink().getUrl(), outputQueue);
            sqs.sendMessage(outputQueue, String.valueOf(article.getId()));
        }
    }

    public String getOutputQueue() {
        return outputQueue;
    }

    public void setOutputQueue(final String outputQueue) {
        this.outputQueue = outputQueue;
    }
}
