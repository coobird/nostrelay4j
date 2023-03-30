package net.coobird.nostr.relay.store.sqlite3;

import net.coobird.nostr.relay.model.Event;
import net.coobird.nostr.relay.model.Filters;
import net.coobird.nostr.relay.store.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class Sqlite3Store implements Store {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    // TODO When instantiated, initialize DB if not available.
    // TODO DB path should be read from the Configurations.
    // TODO Since Configurations doesn't know about Sqlite3Store configs, how to deal with that?

    public Sqlite3Store(Sqlite3StoreConfigurations configuration) {
        LOGGER.info("Using DB path: <{}>", configuration.getDbPath());
    }


    @Override
    public void store(Event event, String rawEvent) {
    }

    @Override
    public List<String> find(List<Filters> filters) {
        return null;
    }
}
