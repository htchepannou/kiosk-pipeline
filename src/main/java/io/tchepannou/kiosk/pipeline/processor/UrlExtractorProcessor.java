package io.tchepannou.kiosk.pipeline.processor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.tchepannou.kiosk.pipeline.model.Feed;
import io.tchepannou.kiosk.pipeline.service.HttpService;
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

@ConfigurationProperties("kiosk.pipeline.UrlExtractorProcessor")
public class UrlExtractorProcessor extends SQSProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlExtractorProcessor.class);

    @Autowired
    AmazonSQS sqs;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    HttpService http;

    private String inputQueue;
    private String outputQueue;

    @Override
    protected void process(final String msg) throws IOException {
        final Feed feed = objectMapper.readValue(msg, Feed.class);
        final List<String> urls = extractUrls(feed);
        for (final String url : urls) {
            LOGGER.info("Sending {} to {}", url, outputQueue);
            sqs.sendMessage(outputQueue, url);
        }
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

    //-- Getter/Setter
    @Override
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
