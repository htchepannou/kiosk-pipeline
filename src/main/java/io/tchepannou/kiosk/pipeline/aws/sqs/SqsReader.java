package io.tchepannou.kiosk.pipeline.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.google.common.annotations.VisibleForTesting;
import io.tchepannou.kiosk.pipeline.service.ThreadMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqsReader implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SqsReader.class);
    private static int threadCount = 0;

    private final String queueName;
    private final AmazonSQS sqs;
    private final SqsConsumer consumer;
    private final ThreadMonitor monitor;
    long minDelay = 1000 * 10;
    long maxDelay = 60000 * 1;

    //-- Constructor
    public SqsReader(
            final String queueName,
            final AmazonSQS sqs,
            final SqsConsumer consumer,
            final ThreadMonitor monitor
    ) {
        this.queueName = queueName;
        this.sqs = sqs;
        this.consumer = consumer;
        this.monitor = monitor;
    }

    //-- Runnable
    public static void start(
            final String queueName,
            final AmazonSQS sqs,
            final SqsConsumer consumer,
            final ThreadMonitor monitor,
            final long maxDelayMillis
    ) {
        final SqsReader reader = new SqsReader(queueName, sqs, consumer, monitor);
        reader.setMaxDelay(maxDelayMillis);
        final Thread thread = new Thread(reader);
        thread.setDaemon(true);
        thread.setName(consumer.getClass().getSimpleName() + "_" + (++threadCount));
        thread.start();
    }

    @Override
    public void run() {
        long delay = minDelay;
        boolean done = false;

        monitor.started(this);
        try {
            while (!done) {
                try {
                    if (process() <= 0) {
                        delay = sleep(delay);
                        done = delay >= maxDelay;
                    } else {
                        delay = minDelay;
                    }
                } catch (Exception ex){
                    LOGGER.error("Unexpected error", ex);
                }
            }
            LOGGER.info("Done");
        } finally {
            monitor.finished(this);
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

        try {
            LOGGER.info("Sleeping for {} seconds", delay / 1000);
            Thread.sleep(delay);
        } catch (final InterruptedException e) {
            LOGGER.warn("Thread interrupted", e);
        }

        return 2 * delay;
    }

    public long getMinDelay() {
        return minDelay;
    }

    public void setMinDelay(final long minDelay) {
        this.minDelay = minDelay;
    }

    public long getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(final long maxDelay) {
        this.maxDelay = maxDelay;
    }
}
