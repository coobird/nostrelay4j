package net.coobird.nostr.relay.store.sqlite3;

import net.coobird.nostr.relay.store.StoreTestBase;

public class Sqlite3StoreTest extends StoreTestBase<Sqlite3Store> {
    @Override
    protected Sqlite3Store getStore() {
        try {
            return new Sqlite3Store(new Sqlite3StoreConfigurations(":memory:"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
