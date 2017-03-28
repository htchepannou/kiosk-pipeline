package io.tchepannou.kiosk.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MessageQueueProcessor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageQueueProcessor.class);

    private MessageQueue queue;
    private Consumer consumer;
    private Delay delay;
    private CountDownLatch latch;

    public MessageQueueProcessor(
            final MessageQueue queue,
            final Consumer consumer,
            final Delay delay,
            final CountDownLatch latch
    ) {
        this.queue = queue;
        this.consumer = consumer;
        this.delay = delay;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            int count = 0;
            while (true) {
                try {

                    final int processed = process();
                    if (processed == 0) {
                        delay.sleep();
                    } else {
                        delay.reset();
                        count += processed;
                    }

                } catch (InterruptedException e) {

                    break;

                } catch (IOException e) {

                    LOGGER.error("Unexpected error", e);
                    break;
                }
            }

            LOGGER.info("{} message processed from {}", count, queue.getName());
        } finally {
            latch.countDown();
        }
    }

    public int process () throws IOException {
        final List<String> messages = queue.poll();
        int count = 0;

        for (String message : messages){
            try {
                consumer.consume(message);
                count++;
            } catch (Exception e){
                LOGGER.error("Unable to consume {}", message, e);
            }
        }

        return count;
    }
}
