package net.coobird.nostr.relay.messaging;

public interface MessageConsumer<T> {
    // takes a message from the queue, processes it.
    // purpose is to run a pipeline, and notify whoever owns the subscription (webserver that has connection for the subscription)
    void receive(T message);
}
