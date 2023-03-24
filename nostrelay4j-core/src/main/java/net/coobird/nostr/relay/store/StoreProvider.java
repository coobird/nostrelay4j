package net.coobird.nostr.relay.store;

public interface StoreProvider {
    String identifier();
    Store create();
}
