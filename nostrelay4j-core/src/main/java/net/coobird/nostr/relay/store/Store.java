package net.coobird.nostr.relay.store;

import net.coobird.nostr.relay.model.Event;
import net.coobird.nostr.relay.model.Filters;

import java.util.List;

public interface Store {
    void store(Event event, String rawEvent);

    /**
     * Find raw events which satisfy the conditions of filters.
     */
    List<String> find(List<Filters> filters);
}
