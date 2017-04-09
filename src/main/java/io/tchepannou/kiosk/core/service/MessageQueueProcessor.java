package io.tchepannou.kiosk.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class MessageQueueProcessor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageQueueProcessor.class);

    private final MessageQueue queue;
    private final Consumer consumer;
    private final Delay delay;
    private final ThreadCountDown latch;

    public MessageQueueProcessor(
            final MessageQueue queue,
            final Consumer consumer,
            final Delay delay,
            final ThreadCountDown latch
    ) {
        this.queue = queue;
        this.consumer = consumer;
        this.delay = delay;
        this.latch = latch;
    }

    @Override
    public void run() {
        latch.countUp();
        try {
            int count = 0;
            while (true) {
                try {

                    final int processed = process();
                    if (processed == 0) {
                        if (!delay.sleep()){
                            LOGGER.info("Too much wait on <{}>... stopping", queue.getName());
                            break;
                        }
                    } else {
                        delay.reset();
                        count += processed;
                    }

                } catch (final InterruptedException e) {

                    break;

                } catch (final IOException e) {

                    LOGGER.error("Unexpected error", e);
                    break;
                }
            }

            LOGGER.info("{} message processed from {}", count, queue.getName());
        } finally {
            latch.countDown();
        }
    }

    public int process() throws IOException {
        final List<String> messages = queue.poll();
        int count = 0;

        for (final String message : messages) {
            try {
                consumer.consume(message);
                count++;
            } catch (final Exception e) {
                LOGGER.error("Unable to consume <{}>", message, e);
            }
        }

        return count;
    }
}
