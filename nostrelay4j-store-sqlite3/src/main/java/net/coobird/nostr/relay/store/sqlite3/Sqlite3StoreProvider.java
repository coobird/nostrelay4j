package net.coobird.nostr.relay.store.sqlite3;

import net.coobird.nostr.relay.config.ConfigurationManager;
import net.coobird.nostr.relay.store.StoreProvider;

import java.io.IOException;
import java.io.InputStream;

public class Sqlite3StoreProvider implements StoreProvider {
    public static String getIdentifier() {
        return "sqlite3";
    }

    @Override
    public String identifier() {
        return getIdentifier();
    }

    @Override
    public Sqlite3Store create() {
        try (InputStream is = ConfigurationManager.getConfigurationAsStream()) {
            Sqlite3StoreConfigurations configuration = Sqlite3StoreConfigurations.getConfiguration(is);
            return new Sqlite3Store(configuration);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
