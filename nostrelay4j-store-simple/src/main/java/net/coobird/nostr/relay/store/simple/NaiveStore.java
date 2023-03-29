package net.coobird.nostr.relay.store.simple;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.coobird.nostr.relay.model.Event;
import net.coobird.nostr.relay.model.Filters;
import net.coobird.nostr.relay.store.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A {@link HashSet} backed store.
 * Events which are retrived from the store are returned sorted by {@code createdAt} in ascending order.
 */
public class NaiveStore implements Store {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Set<Event> events = new HashSet<>();

    @Override
    public void store(String rawEvent) {
        LOGGER.debug("Received store request.");

        try {
            var event = OBJECT_MAPPER.readValue(rawEvent, Event.class);
            events.add(event);
            LOGGER.debug("Added event: id=<{}>", event.id());

        } catch (JsonProcessingException e) {
            LOGGER.error("Error serializing event.", e);
            throw new RuntimeException("Error serializing event.", e);
        }
    }

    @Override
    public List<String> find(List<Filters> filters) {
        LOGGER.debug("Received find request for filters. filters=<{}>", filters);
        List<Event> matchedEvents = new ArrayList<>();
        for (var event : events) {
            for (var filter : filters) {
                if (!filter.evaluate(event)) {
                    break;
                }
            }
            matchedEvents.add(event);
            LOGGER.debug("Found matched event. event=<{}> filters=<{}>", event, filters);
        }
        Collections.sort(matchedEvents, Comparator.comparing(Event::createdAt));

        var sortedMatchedEvents = matchedEvents.stream()
                .map(event -> {
                    try {
                        return OBJECT_MAPPER.writeValueAsString(event);

                    } catch (JsonProcessingException e) {
                        LOGGER.error("Error deserializing event.", e);
                        throw new RuntimeException("Error deserializing event.", e);
                    }
                }).toList();

        LOGGER.debug("Returning matched events: matchedEvents=<{}>", sortedMatchedEvents);
        return sortedMatchedEvents;
    }
}
