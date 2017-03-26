package io.tchepannou.kiosk.core.service.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import io.tchepannou.kiosk.core.service.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SqsMessageQueue implements MessageQueue {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqsMessageQueue.class);

    @Autowired
    private AmazonSQS sqs;

    private String url;

    @Override
    public void push(final String msg) throws IOException {
        try {
            LOGGER.info("{}: pushing {}", getName(), msg);
            sqs.sendMessage(url, msg);
        } catch (AmazonClientException e){
            throw new IOException(String.format("Unable to send to %s: %s", url, msg), e);
        }
    }

    @Override
    public List<String> poll() throws IOException {
        try {
            final ReceiveMessageResult result = sqs.receiveMessage(
                    new ReceiveMessageRequest()
                            .withQueueUrl(url)
                            .withMaxNumberOfMessages(10)
                            .withWaitTimeSeconds(20)
            );
            final List<String> bodies = new ArrayList<>();
            for (final Message msg : result.getMessages()) {
                sqs.deleteMessage(url, msg.getReceiptHandle());
                bodies.add(msg.getBody());
            }

            return bodies;

        } catch (final RuntimeException e) {
            throw new IOException(String.format("Unable to pool message from %s", url), e);
        }
    }

    @Override
    public String getName (){
        return url.substring(url.lastIndexOf('/')+1);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }
}
