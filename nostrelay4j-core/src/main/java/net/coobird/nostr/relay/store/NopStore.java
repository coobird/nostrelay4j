package net.coobird.nostr.relay.store;

import net.coobird.nostr.relay.model.Event;
import net.coobird.nostr.relay.model.Filters;

import java.util.Collections;
import java.util.List;

public class NopStore implements Store {
    @Override
    public void store(Event event, String rawEvent) {
        // No-op
    }

    @Override
    public List<String> find(List<Filters> filters) {
        return Collections.emptyList();
    }
}
