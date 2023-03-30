package net.coobird.nostr.relay.store;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.coobird.nostr.relay.model.Event;
import net.coobird.nostr.relay.model.Filters;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class StoreTestBase<T extends Store> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Return a {@link Store} that's initialized with no events stored.
     * @return A without any events stored.
     */
    protected abstract T getStore();

    @Test
    public void storeSmokeTest() throws Exception {
        Store store = getStore();

        String inputEvent = """
                {"id":"0000","pubkey":"1111","created_at":1234567890,"kind":1,"tags":[],"content":"hello world","sig":"2222"}
                """.strip();
        store.store(OBJECT_MAPPER.readValue(inputEvent, Event.class), inputEvent);

        List<String> returnedEvents = store.find(Collections.singletonList(
                new Filters(
                        Collections.singletonList("0000"),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null,
                        null,
                        null
                )
        ));

        assertEquals(1, returnedEvents.size());
        assertJsonEquals(inputEvent, returnedEvents.get(0));
    }

    @Test
    public void findWhenEmptyReturnsNothing() {
        Store store = getStore();

        List<String> returnedEvents = store.find(Collections.singletonList(
                new Filters(
                        Collections.singletonList("0000"),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null,
                        null,
                        null
                )
        ));

        assertEquals(0, returnedEvents.size());
    }

    @Test
    public void findWithFilterThatDoesntMatchReturnsNothing() throws Exception {
        Store store = getStore();

        String inputEvent = """
                {"id":"0000","pubkey":"1111","created_at":1234567890,"kind":1,"tags":[],"content":"hello world","sig":"2222"}
                """.strip();
        store.store(OBJECT_MAPPER.readValue(inputEvent, Event.class), inputEvent);

        List<String> returnedEvents = store.find(Collections.singletonList(
                new Filters(
                        Collections.singletonList("1111"),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null,
                        null,
                        null
                )
        ));

        assertEquals(0, returnedEvents.size());
    }

    private static void assertJsonEquals(String expected, String actual) throws JsonProcessingException {
        assertEquals(OBJECT_MAPPER.readTree(expected), OBJECT_MAPPER.readTree(actual));
    }
}
