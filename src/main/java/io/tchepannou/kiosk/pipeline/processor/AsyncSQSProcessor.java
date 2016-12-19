package io.tchepannou.kiosk.pipeline.processor;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Executor;

public abstract class AsyncSQSProcessor extends SQSProcessor {
    @Autowired
    Executor executor;

    public void process() {
        final String inputQueue = getInputQueue();
        final ReceiveMessageRequest request = createReceiveMessageRequest();

        while (true) {
            final ReceiveMessageResult result = sqs.receiveMessage(request);
            if (result.getMessages().isEmpty()) {
                break;
            }

            for (final Message message : result.getMessages()) {
                executor.execute(() -> process(inputQueue, message));
            }
        }
    }
}
