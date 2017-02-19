package io.tchepannou.kiosk.pipeline.consumer;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.aws.sqs.SqsConsumer;
import io.tchepannou.kiosk.pipeline.persistence.domain.Feed;
import io.tchepannou.kiosk.pipeline.persistence.domain.Link;
import io.tchepannou.kiosk.pipeline.persistence.repository.FeedRepository;
import io.tchepannou.kiosk.pipeline.persistence.repository.LinkRepository;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import io.tchepannou.kiosk.pipeline.service.UrlBlacklistService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigurationProperties("kiosk.pipeline.UrlExtractorConsumer")
public class UrlExtractorConsumer implements SqsConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlExtractorConsumer.class);

    @Autowired
    AmazonSQS sqs;

    @Autowired
    FeedRepository feedRepository;

    @Autowired
    HttpService http;

    @Autowired
    LinkRepository linkRepository;

    @Autowired
    UrlBlacklistService urlBlacklistService;

    private String inputQueue;
    private String outputQueue;

    //-- SqsConsumer overrides
    @Override
    public void consume(final String body) throws IOException {
        final Feed feed = feedRepository.findOne(Long.parseLong(body));
        final List<String> urls = extractUrls(feed);
        for (String url : urls) {
            url = normalize(url);
            if (isHomePage(url, feed)) {
                LOGGER.info("{} is home page", url);
                continue;
            }
            if (alreadyDownloaded(url)) {
                LOGGER.info("{} already downloaded", url);
                continue;
            }

            if (!urlBlacklistService.contains(url)) {
                LOGGER.info("Sending message <{}> to queue: {}", url, outputQueue);
                sqs.sendMessage(outputQueue, url);
            } else {
                LOGGER.info("{} is blacklisted", url);
            }
        }
    }

    //-- Private
    private String normalize(final String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1).toLowerCase() : url.toLowerCase();
    }

    private boolean isHomePage(final String url, final Feed feed) {
        final String xurl = url.toLowerCase();
        final String feedUrl = feed.getUrl().toLowerCase();
        return xurl.equals(feedUrl) || xurl.equals(feedUrl + "/");
    }

    private boolean alreadyDownloaded(final String url) {
        final String keyhash = Link.hash(url);
        return linkRepository.findByUrlHash(keyhash) != null;
    }

    private List<String> extractUrls(final Feed feed) throws IOException {
        LOGGER.info("Extracting URL from {}", feed.getUrl());

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        http.get(feed.getUrl(), out);

        final String html = out.toString("utf-8");
        final Document doc = Jsoup.parse(html);
        doc.setBaseUri(feed.getUrl());
        final Set<String> urls = doc.select("a")
                .stream()
                .map(e -> e.attr("abs:href"))
                .filter(href -> feed.urlMatches(href))
                .collect(Collectors.toSet());

        return new ArrayList<>(urls);
    }

    public String getInputQueue() {
        return inputQueue;
    }

    public void setInputQueue(final String inputQueue) {
        this.inputQueue = inputQueue;
    }

    public String getOutputQueue() {
        return outputQueue;
    }

    public void setOutputQueue(final String outputQueue) {
        this.outputQueue = outputQueue;
    }
}
