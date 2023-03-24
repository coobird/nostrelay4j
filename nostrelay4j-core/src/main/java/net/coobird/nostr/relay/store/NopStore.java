package net.coobird.nostr.relay.store;

import net.coobird.nostr.relay.model.Filters;

import java.util.Collections;
import java.util.List;

public class NopStore implements Store {
    @Override
    public void store(String rawEvent) {
        // No-op
    }

    @Override
    public List<String> find(List<Filters> filters) {
        return Collections.emptyList();
    }
}
