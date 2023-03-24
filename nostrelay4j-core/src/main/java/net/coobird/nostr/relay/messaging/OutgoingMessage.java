package net.coobird.nostr.relay.messaging;

public record OutgoingMessage(Object owner, Type type, String subscriptionId, String content) {
    public enum Type {
        NOTICE,
        EVENT,
        CLOSE
    }
}
