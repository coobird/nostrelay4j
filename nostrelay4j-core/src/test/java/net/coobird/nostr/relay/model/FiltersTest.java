package net.coobird.nostr.relay.model;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class FiltersTest {
    private static final Event SAMPLE_EVENT = new Event(
            "0000",
            "1111",
            1234567890,
            1,
            Collections.emptyList(),
            "Hello world",
            "2222"
    );

    private static Stream<Arguments> provideIdsAndExpectations() {
        return Stream.of(
                Arguments.of(Collections.emptyList(), false),
                Arguments.of(null, false),
                Arguments.of(Collections.singletonList("0000"), true), // exact match
                Arguments.of(Collections.singletonList("00"), true),   // partial match
                Arguments.of(Collections.singletonList(""), false),     // ... choice that empty string matches nothing
                Arguments.of(Collections.singletonList("asdf"), false)  // completely not matching
        );
    }

    @ParameterizedTest
    @MethodSource("provideIdsAndExpectations")
    void idsFiltersTests(List<String> ids, boolean expected) {
        assertEquals(
                expected,
                new Filters(
                        ids,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                ).evaluate(SAMPLE_EVENT)
        );
    }
}