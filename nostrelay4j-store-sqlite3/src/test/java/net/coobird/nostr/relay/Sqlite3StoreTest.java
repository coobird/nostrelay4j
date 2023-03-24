package net.coobird.nostr.relay;

import net.coobird.nostr.relay.store.sqlite3.Sqlite3Store;
import net.coobird.nostr.relay.store.sqlite3.Sqlite3StoreConfigurations;
import org.junit.jupiter.api.Test;

public class Sqlite3StoreTest {
    @Test
    public void foo() {
        new Sqlite3Store(new Sqlite3StoreConfigurations());
    }
}
