package net.coobird.nostr.relay.store.simple;

import net.coobird.nostr.relay.store.StoreTestBase;

public class NaiveStoreTest extends StoreTestBase<NaiveStore> {
    @Override
    protected NaiveStore getStore() {
        return new NaiveStore();
    }
}
