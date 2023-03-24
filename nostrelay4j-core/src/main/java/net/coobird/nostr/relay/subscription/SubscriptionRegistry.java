package net.coobird.nostr.relay.subscription;

import net.coobird.nostr.relay.model.Filters;

import java.util.List;
import java.util.Map;

public interface SubscriptionRegistry {
    /** called on REQ message */
    void addSubscription(Owner owner, String subscriptionId, List<Filters> filters);
    /** called on CLOSE message or when websocket closed. */
    void removeSubscription(Owner owner, String subscriptionId);

    /**
     *
     * @return a map of subscriptionId to filters
     */
    Map<String, List<Filters>> getAllSubscriptions();

    /**
     * Get subscriptions associated with an owner.
     * Intent of using this is to limit getting subscriptions to a subset, so that not all filters need to be evaluated.
     *
     * @param owner
     * @return
     */
    Map<String, List<Filters>> getSubscriptions(Owner owner);

    /**
     * Once a subscription is known, find the owner so that the message can be delivered to the right owner.
     * @param subscriptionId
     * @return
     */
    Owner getOwner(String subscriptionId);

    void unsubscribeAllForOwner(Owner owner);

    int getSubscriptionCount();
    int getSessionCount();
}
