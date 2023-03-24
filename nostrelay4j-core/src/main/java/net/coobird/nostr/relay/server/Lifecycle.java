package net.coobird.nostr.relay.server;

public interface Lifecycle {
    void start() throws Exception;
    void stop() throws Exception;
}
