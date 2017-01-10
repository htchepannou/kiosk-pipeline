package io.tchepannou.kiosk.pipeline.aws.sqs;

import java.io.IOException;

public interface SqsConsumer {
    String getInputQueue();
    void consume(String body) throws IOException;
}
