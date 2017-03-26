package io.tchepannou.kiosk.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class MessageQueueProcessor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageQueueProcessor.class);

    private MessageQueue queue;
    private Consumer consumer;
    private Delay delay;

    public MessageQueueProcessor(
            final MessageQueue queue,
            final Consumer consumer,
            final Delay delay
    ) {
        this.queue = queue;
        this.consumer = consumer;
        this.delay = delay;
    }

    @Override
    public void run() {
        int count = 0;
        while (true){
            try {

                final int processed = process();
                if (processed == 0) {
                    delay.sleep();
                } else {
                    delay.reset();
                    count += processed;
                }

            } catch(InterruptedException e){

                break;

            } catch (IOException e){

                LOGGER.error("Unexpected error", e);
                break;
            }
        }

        LOGGER.info("{} message processed from {}", count, queue.getName());
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
