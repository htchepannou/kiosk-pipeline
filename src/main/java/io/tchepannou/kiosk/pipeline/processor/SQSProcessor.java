package io.tchepannou.kiosk.pipeline.processor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import io.tchepannou.kiosk.pipeline.service.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public abstract class SQSProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SQSProcessor.class);

    @Autowired
    AmazonSQS sqs;

    @Autowired
    HttpService http;

    public void process() {
        final String inputQueue = getInputQueue();
        final ReceiveMessageRequest request = createReceiveMessageRequest();

        while (true) {
            final ReceiveMessageResult result = sqs.receiveMessage(request);
            if (result.getMessages().isEmpty()) {
                break;
            }

            for (final Message message : result.getMessages()) {
                try {
                    process(message.getBody());
                    sqs.deleteMessage(inputQueue, message.getReceiptHandle());
                } catch (final Exception e) {
                    LOGGER.error("Unexpected error when processing Message #{}", message.getMessageId(), e);
                }
            }
        }
    }

    private ReceiveMessageRequest createReceiveMessageRequest() {
        final String inputQueue = getInputQueue();
        return new ReceiveMessageRequest()
                .withQueueUrl(inputQueue)
                .withMaxNumberOfMessages(10)
                .withWaitTimeSeconds(20);
    }

    protected abstract void process(final String body) throws IOException;

    public abstract String getInputQueue();
}
