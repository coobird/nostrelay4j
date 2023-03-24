package net.coobird.nostr.relay.subscription;

import net.coobird.nostr.relay.model.Filters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultSubscriptionRegistry implements SubscriptionRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<Owner, List<String>> ownerToSubscriptionIds = new HashMap<>();
    private final Map<String, List<Filters>> subscriptionIdToFilters = new HashMap<>();
    private final Map<String, Owner> subscriptionIdToOwner = new HashMap<>();

    private <T> T guardedAction(Callable<T> r) throws Exception {
        LOGGER.trace("wait for lock");
        lock.lock();
        LOGGER.trace("acquired lock");
        var returnValue = r.call();
        lock.unlock();
        LOGGER.trace("released lock");
        return returnValue;
    }

    @Override
    public void addSubscription(Owner owner, String subscriptionId, List<Filters> filters) {
        LOGGER.debug("Add subscription <{}> for session <{}> with filters <{}>", subscriptionId, owner, filters);

        try {
            guardedAction(() -> {
                subscriptionIdToFilters.put(subscriptionId, filters);

                var subscriptions = ownerToSubscriptionIds.getOrDefault(owner, new ArrayList<>());
                subscriptions.add(subscriptionId);
                ownerToSubscriptionIds.put(owner, subscriptions);

                subscriptionIdToOwner.put(subscriptionId, owner);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeSubscription(Owner owner, String subscriptionId) {
        LOGGER.debug("Remove subscription <{}> for session <{}>", subscriptionId, owner);

        try {
            guardedAction(() -> {
                var subscriptions = ownerToSubscriptionIds.getOrDefault(owner, new ArrayList<>());
                subscriptions.remove(subscriptionId);

                if (subscriptions.isEmpty()) {
                    ownerToSubscriptionIds.remove(owner);
                } else {
                    ownerToSubscriptionIds.put(owner, subscriptions);
                }

                subscriptionIdToFilters.remove(subscriptionId);
                subscriptionIdToOwner.remove(subscriptionId);
                return null;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<Filters>> getSubscriptions(Owner owner) {
        try {
            return guardedAction(() -> {
                return ownerToSubscriptionIds.getOrDefault(owner, Collections.emptyList()).stream()
                        .collect(Collectors.toMap(Function.identity(), subscriptionIdToFilters::get));
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, List<Filters>> getAllSubscriptions() {
        try {
            return guardedAction(() -> new HashMap<>(subscriptionIdToFilters));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Owner getOwner(String subscriptionId) {
        return subscriptionIdToOwner.get(subscriptionId);
    }

    @Override
    public void unsubscribeAllForOwner(Owner owner) {
        LOGGER.debug("Unsubscribe subscriptions for owner: {}", owner);

        try {
            guardedAction(() -> {
                ownerToSubscriptionIds.getOrDefault(owner, Collections.emptyList())
                        .forEach(subscriptionId -> {
                            subscriptionIdToFilters.remove(subscriptionId);
                            subscriptionIdToOwner.remove(subscriptionId);
                        });
                ownerToSubscriptionIds.remove(owner);

                LOGGER.trace("after remove: sessions=<{}> hashCode=<{}>", ownerToSubscriptionIds, ownerToSubscriptionIds.hashCode());
                return null;
            });
        } catch (Exception e) {
            LOGGER.error("oh shit", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getSubscriptionCount() {
        LOGGER.debug("state is locked? <{}>", lock.isLocked());
        LOGGER.debug("subscriptionIdToOwner: <{}>", subscriptionIdToOwner);
        return subscriptionIdToFilters.values().stream()
                .map(List::size)
                .reduce(Integer::sum)
                .orElse(0);
    }

    @Override
    public int getSessionCount() {
        LOGGER.info("owners: <{}>", ownerToSubscriptionIds);
        return ownerToSubscriptionIds.size();
    }
}
