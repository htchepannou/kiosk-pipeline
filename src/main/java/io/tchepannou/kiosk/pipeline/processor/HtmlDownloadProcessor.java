package io.tchepannou.kiosk.pipeline.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ConfigurationProperties("kiosk.pipeline.DownloadWebPageProcessor")
public class HtmlDownloadProcessor extends SQSProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlDownloadProcessor.class);

    private String inputQueue;
    private String outputS3Bucket;
    private String outputS3Key;
    private int threadPoolSize;
    private ExecutorService executorService;

    //-- Public
    @PostConstruct
    public void init (){
        executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    //-- SQSProcessor overrides
    @Override
    protected void process(final String body) throws IOException {
    }

    //-- Getter/Setter
    @Override
    public String getInputQueue() {
        return inputQueue;
    }

    public void setInputQueue(final String inputQueue) {
        this.inputQueue = inputQueue;
    }

    public String getOutputS3Bucket() {
        return outputS3Bucket;
    }

    public void setOutputS3Bucket(final String outputS3Bucket) {
        this.outputS3Bucket = outputS3Bucket;
    }

    public String getOutputS3Key() {
        return outputS3Key;
    }

    public void setOutputS3Key(final String outputS3Key) {
        this.outputS3Key = outputS3Key;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(final int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }
}
