package io.tchepannou.kiosk.pipeline.aws.sqs;

import java.io.IOException;

@Deprecated
public interface SqsConsumer {
    String getInputQueue();
    void consume(String body) throws IOException;
}
