package io.tchepannou.kiosk.pipeline.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import io.tchepannou.kiosk.pipeline.service.ThreadMonitor;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.HashMap;
import java.util.Map;

public class SqsConsumerGroup {
    public static final int ONE_MINUTE = 60 * 1000;

    private final AmazonSQS sqs;
    private final ThreadMonitor threadMonitor;
    private final ConfigurableApplicationContext applicationContext;

    int maxThreadWait = 5 * ONE_MINUTE;
    int maxDuration = 30 * ONE_MINUTE;
    private final Map<Class<? extends SqsConsumer>, Integer> workersByConsummer = new HashMap<>();

    public SqsConsumerGroup(
            final AmazonSQS sqs,
            final ThreadMonitor threadMonitor,
            final ConfigurableApplicationContext applicationContext
    ) {
        this.sqs = sqs;
        this.threadMonitor = threadMonitor;
        this.applicationContext = applicationContext;
    }

    //-- Public
    public void add(final Class<? extends SqsConsumer> consumer, final int workers) {
        workersByConsummer.put(consumer, workers);
    }

    public void consume() {
        for (final Class<? extends SqsConsumer> consumerClass : workersByConsummer.keySet()) {
            consume(consumerClass);
        }
        threadMonitor.waitAllThreads(ONE_MINUTE, maxDuration);
    }

    //-- Private
    private void consume(final Class<? extends SqsConsumer> consumerClass) {

        final int workers = workersByConsummer.get(consumerClass);
        for (int i = 0; i < workers; i++) {
            final SqsConsumer consumer = applicationContext.getBean(consumerClass);
            SqsReader.start(consumer.getInputQueue(), sqs, consumer, threadMonitor, maxThreadWait);
        }

    }

    //-- Getter/Setter
    public int getMaxThreadWait() {
        return maxThreadWait;
    }

    public void setMaxThreadWait(final int maxThreadWait) {
        this.maxThreadWait = maxThreadWait;
    }

    public int getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(final int maxDuration) {
        this.maxDuration = maxDuration;
    }
}
