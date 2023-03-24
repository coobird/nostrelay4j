package net.coobird.nostr.relay.subscription;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.coobird.nostr.relay.server.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SubscriptionMonitor implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final ScheduledExecutorService es;
    private final SubscriptionRegistry subscriptionManager;

    public SubscriptionMonitor(SubscriptionRegistry subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
        this.es = Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder().setNameFormat("subscription-monitor-%d").build()
        );
    }

    @Override
    public void start() throws Exception {
        LOGGER.info("Started subscription monitor.");
        es.scheduleAtFixedRate(() -> {
            LOGGER.info(
                    "Counts: session=<{}> subscription=<{}>",
                    subscriptionManager.getSessionCount(),
                    subscriptionManager.getSubscriptionCount()
            );
        }, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void stop() throws Exception {
        es.shutdown();
    }
}
