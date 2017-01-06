package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsS3Consumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Article;
import io.tchepannou.kiosk.pipeline.persistence.repository.ArticleRepository;
import io.tchepannou.kiosk.pipeline.service.similarity.Pair;
import io.tchepannou.kiosk.pipeline.service.similarity.SimilarityService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
public class ArticleDedupConsumer extends SqsS3Consumer {
    private final Logger LOGGER = LoggerFactory.getLogger(ArticleDedupConsumer.class);

    @Autowired
    AmazonS3 s3;

    @Autowired
    AmazonSQS sqs;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    SimilarityService similarityService;

    private String inputQueue;
    private float similarityThreshold = 0.9f;

    @Override
    protected void consume(final S3Object s3Object) throws IOException {
        LOGGER.info("Performing Dedup");

        final File file = File.createTempFile("matrix", ".txt");
        try {
            // Download
            try (final FileOutputStream fout = new FileOutputStream(file)) {
                LOGGER.info("Downloading s3://{}/{} to {}", s3Object.getBucketName(), s3Object.getKey(), file.getAbsolutePath());
                IOUtils.copy(s3Object.getObjectContent(), fout);
            }

            // Extract dedups
            try (final FileInputStream fin = new FileInputStream(file)) {

                /* find the dedups cluster */
                final Collection<Pair> pairs = similarityService.filter(fin, similarityThreshold, Integer.MAX_VALUE);
                final List<Set<Long>> clusters = Pair.clusterize(pairs);

                /* Store */
                store(s3Object.getBucketName(), key(s3Object.getKey()), clusters);

                /* update status of articles */
                updateStatus(clusters);
            }
        } finally {
            file.delete();
        }
    }

    //-- Private
    private String key(final String matrixKey) {
        final int i = matrixKey.lastIndexOf('.');
        return matrixKey.substring(0, i) + "-dedup.txt";
    }

    private void store(final String bucket, final String key, final List<Set<Long>> clusters) {
        final StringBuilder buff = new StringBuilder();
        for (final Set<Long> cluster : clusters) {
            final List<String> str = cluster.stream().map(i -> String.valueOf(i)).collect(Collectors.toList());
            final String line = String.join(",", str);
            buff.append(line).append('\n');
        }

        final ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(buff.length());
        meta.setContentType("text/plain");

        LOGGER.info("Storing dedup to s3://{}/{}", bucket, key);
        s3.putObject(bucket, key, new ByteArrayInputStream(buff.toString().getBytes()), meta);
    }

    final void updateStatus(final List<Set<Long>> clusters) {

        /* get the articles */
        final Set<Long> ids = extractIds(clusters);
        final Iterable<Article> articles = articleRepository.findAll(ids);
        final Map<Long, Article> articlesById = new HashMap<>();
        for (final Article article : articles) {
            articlesById.put(article.getId(), article);
        }

        /* dedup */
        final List<Article> persist = new ArrayList<>();
        final Comparator<Long> comparator = (i1, i2) ->
                articlesById.get(i1).getPublishedDate().compareTo(articlesById.get(i2).getPublishedDate());
        for (final Set<Long> cluster : clusters) {
            final List<Long> articleIds = new ArrayList<>(cluster);
            Collections.sort(articleIds, comparator);

            final long articleId = articleIds.get(0);

            for (final Long id : articleIds) {
                final Article article = articlesById.get(id);
                if (article == null || id == articleId) {
                    continue;
                } else if (!article.isDuplicate()) {
                    article.setStatus(Article.STATUS_DUPLICATE);
                    article.setDuplicateId(articleId);
                    persist.add(article);
                }
            }
        }

        /* update */
        articleRepository.save(persist);
    }

    private Set<Long> extractIds(final Collection<Set<Long>> clusters) {
        final Set<Long> ids = new HashSet<>();
        for (final Set<Long> cluster : clusters) {
            ids.addAll(cluster);
        }
        return ids;
    }

    //-- Getter/Setter

    public String getInputQueue() {
        return inputQueue;
    }

    public void setInputQueue(final String inputQueue) {
        this.inputQueue = inputQueue;
    }

    public float getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(final float similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }
}
