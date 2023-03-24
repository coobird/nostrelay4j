package net.coobird.nostr.relay.messaging;

public interface MessageProducer<T> {
    // simply, sends the event to consumer
    void send(T message);
}
