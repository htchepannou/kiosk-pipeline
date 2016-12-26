package io.tchepannou.kiosk.pipeline.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqsReader implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqsReader.class);

    private final String queueName;
    private final AmazonSQS sqs;
    private final SqsConsumer consumer;
    long minDelay = 1000 * 30;
    long maxDelay = 60000 * 1;

    //-- Constructor
    public SqsReader(final String queueName, final AmazonSQS sqs, final SqsConsumer consumer) {
        this.queueName = queueName;
        this.sqs = sqs;
        this.consumer = consumer;
    }

    //-- Runnable
    public static void start (final String queueName, final AmazonSQS sqs, final SqsConsumer consumer){
        final SqsReader reader = new SqsReader(queueName, sqs, consumer);
        final Thread thread = new Thread(reader);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        long delay = minDelay;
        boolean done = false;
        try {
            while (!done) {
                if (process() <= 0) {
                    delay = sleep(delay);
                    done = delay >= maxDelay;
                } else {
                    delay = minDelay;
                }
            }
        } catch (final Exception ex) {
            LOGGER.error("Unexpected error", ex);
        }
    }

    //-- Private
    @VisibleForTesting
    protected int process() {
        final ReceiveMessageRequest request = createReceiveMessageRequest(queueName);
        final ReceiveMessageResult result = sqs.receiveMessage(request);
        int count = 0;
        for (final Message message : result.getMessages()) {
            try {

                consumer.consume(message.getBody());
                sqs.deleteMessage(queueName, message.getReceiptHandle());
                count++;

            } catch (final Exception e) {
                LOGGER.error("Unexpected error when processing Message#{}:\n{}", message.getReceiptHandle(), message.getBody(), e);
            }
        }
        return count;
    }

    private ReceiveMessageRequest createReceiveMessageRequest(final String queueName) {
        return new ReceiveMessageRequest()
                .withQueueUrl(queueName)
                .withMaxNumberOfMessages(10)
                .withWaitTimeSeconds(20);
    }

    private long sleep(final long delay) {
        long millis = 2 * delay;

        try {
            LOGGER.error("Sleeping for {} ms", millis);
            Thread.sleep(millis);
        } catch (final InterruptedException e) {
            millis = maxDelay + 1;
        }

        return millis;
    }

}
