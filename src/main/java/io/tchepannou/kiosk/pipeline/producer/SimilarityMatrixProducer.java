package io.tchepannou.kiosk.pipeline.producer;

import com.amazonaws.services.s3.AmazonS3;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.service.similarity.ArticleDocumentFactory;
import io.tchepannou.kiosk.pipeline.service.similarity.Document;
import io.tchepannou.kiosk.pipeline.service.similarity.SimilarityService;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SimilarityMatrixProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimilarityMatrixProducer.class);

    @Autowired
    AmazonS3 s3;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    ArticleDocumentFactory articleDocumentFactory;

    @Autowired
    SimilarityService similarityService;

    @Autowired
    Clock clock;

    private String s3Bucket;
    private String s3Key;

    public void produce() throws IOException {
        LOGGER.info("Generating the similarity matrix");

        final List<Document> docs = loadDocuments();
        final File file = File.createTempFile("similarity", ".txt");
        try (final OutputStream fout = new FileOutputStream(file)) {
            similarityService.compute(docs, fout);

            // Store the report
            final DateFormat prefixFmt = new SimpleDateFormat("yyyy/MM/dd");
            final DateFormat suffixFmt = new SimpleDateFormat("HH");
            final Date now = new Date(clock.millis());
            final String key = String.format("%s/%s/matrix_%s.txt", s3Key, prefixFmt.format(now), suffixFmt.format(now));

            LOGGER.info("Storing {} to s3://{}/{}", file.getAbsolutePath(), s3Bucket, key);
            s3.putObject(s3Bucket, key, file);
        } finally {
            file.delete();
        }
    }

    private List<Document> loadDocuments() {
        final Date endDate = new Date();
        final Date startDate = DateUtils.addDays(endDate, -7);
        final List<Article> articles = articleRepository.findByStatusNotAndPublishedDateBetween(Article.STATUS_INVALID, startDate, endDate);
        return articles.stream()
                .map(a -> articleDocumentFactory.createDocument(a))
                .filter(d -> d != null)
                .collect(Collectors.toList());
    }

    public String getS3Bucket() {
        return s3Bucket;
    }

    public void setS3Bucket(final String s3Bucket) {
        this.s3Bucket = s3Bucket;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(final String s3Key) {
        this.s3Key = s3Key;
    }
}
