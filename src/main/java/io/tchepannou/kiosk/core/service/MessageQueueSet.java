package io.tchepannou.kiosk.core.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MessageQueueSet implements MessageQueue {
    private String name;
    private Collection<MessageQueue> queues = new ArrayList<>();

    public MessageQueueSet(final String name, final Collection<MessageQueue> queues) {
        this.name = name;
        this.queues = queues;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void push(final String msg) throws IOException {
        for (final MessageQueue queue : queues){
            queue.push(msg);
        }
    }

    @Override
    public List<String> poll() throws IOException {
        return Collections.emptyList();
    }
}
