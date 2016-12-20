package io.tchepannou.kiosk.pipeline.aws.sqs;

import java.io.IOException;

public interface SqsConsumer {
    void consume(String body) throws IOException;
}
