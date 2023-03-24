package net.coobird.nostr.relay.messaging;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class MessageQueue<T> {
    private final Deque<T> eventQueue = new ConcurrentLinkedDeque<>();

    public void add(T message) {
        eventQueue.add(message);
    }

    public T remove() {
        return eventQueue.remove();
    }

    public boolean isEmpty() {
        return eventQueue.isEmpty();
    }
}
