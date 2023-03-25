package net.coobird.nostr.relay.store.simple;

import net.coobird.nostr.relay.store.StoreProvider;

public class NaiveStoreProvider implements StoreProvider {
    public static String getIdentifier() {
        return "simple";
    }

    @Override
    public String identifier() {
        return getIdentifier();
    }

    @Override
    public NaiveStore create() {
        return new NaiveStore();
    }
}
