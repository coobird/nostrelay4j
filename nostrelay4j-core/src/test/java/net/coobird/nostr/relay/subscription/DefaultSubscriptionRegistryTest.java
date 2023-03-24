package net.coobird.nostr.relay.subscription;

import net.coobird.nostr.relay.model.Filters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultSubscriptionRegistryTest {
    private SubscriptionRegistry subscriptionRegistry;

    @BeforeEach
    public void setup() {
        this.subscriptionRegistry = new DefaultSubscriptionRegistry();
    }

    @Test
    public void addSubscriptionTest() throws Exception {
        var owner = new Owner(new Object());
        var filters = List.of(
                new Filters(List.of("abcd"), null, null, null, null, null, null, null),
                new Filters(List.of("ef01"), null, null, null, null, null, null, null)
        );

        subscriptionRegistry.addSubscription(owner, "012345678", filters);

        assertEquals(Map.of("012345678", filters), subscriptionRegistry.getSubscriptions(owner));
    }

    @Test
    public void removeSubscription_singleSubscription_validId_OK() throws Exception {
        var owner = new Owner(new Object());
        var filters = List.of(
                new Filters(List.of("abcd"), null, null, null, null, null, null, null),
                new Filters(List.of("ef01"), null, null, null, null, null, null, null)
        );

        subscriptionRegistry.addSubscription(owner, "012345678", filters);
        subscriptionRegistry.removeSubscription(owner, "012345678");

        assertEquals(Collections.emptyMap(), subscriptionRegistry.getSubscriptions(owner));
    }

    @Test
    public void removeSubscription_multipleSubscription_validSession_validId_OK() throws Exception {
        var owner = new Owner(new Object());
        var filters1 = List.of(
                new Filters(List.of("abcd"), null, null, null, null, null, null, null),
                new Filters(List.of("ef01"), null, null, null, null, null, null, null)
        );
        var filters2 = List.of(
                new Filters(List.of("cdef"), null, null, null, null, null, null, null),
                new Filters(List.of("3456"), null, null, null, null, null, null, null)
        );

        subscriptionRegistry.addSubscription(owner, "0123456789", filters1);
        subscriptionRegistry.addSubscription(owner, "abcdef0123", filters2);
        subscriptionRegistry.removeSubscription(owner, "0123456789");

        assertEquals(Map.of("abcdef0123", filters2), subscriptionRegistry.getSubscriptions(owner));
    }
}