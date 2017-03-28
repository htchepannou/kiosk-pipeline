package io.tchepannou.kiosk.pipeline.aws.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

@Deprecated
public abstract class SqsSnsConsumer implements SqsConsumer {
    @Autowired
    ObjectMapper objectMapper;

    protected abstract void consumeMessage(final String message) throws IOException;

    @Override
    public void consume(final String body) throws IOException {
        final Map snsNotification = (Map) objectMapper.readValue(body, Object.class);
        final Object message = snsNotification.get("Message");
        if (message == null) {
            return;
        }

        consumeMessage(message.toString());
    }

}
