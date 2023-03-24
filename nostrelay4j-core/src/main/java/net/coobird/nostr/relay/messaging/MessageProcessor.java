package net.coobird.nostr.relay.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.coobird.nostr.relay.model.Event;
import net.coobird.nostr.relay.model.Filters;
import net.coobird.nostr.relay.server.Lifecycle;
import net.coobird.nostr.relay.store.Store;
import net.coobird.nostr.relay.subscription.SubscriptionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MessageProcessor implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService processingPool = Executors.newFixedThreadPool(
            4,
            new ThreadFactoryBuilder()
                    .setNameFormat("message-processor-%d")
                    .setUncaughtExceptionHandler((t, e) -> {
                        LOGGER.error("Uncaught exception t=<{}> e=<{}>", t, e);
                    })
                    .build()
    );

    private final MessageQueue<IncomingMessage> incomingMessageQueue;
    private final MessageQueue<OutgoingMessage> outgoingMessageQueue;
    private final Store store;
    private final SubscriptionRegistry subscriptionRegistry;

    public MessageProcessor(MessageQueue<IncomingMessage> incomingMessageQueue,
                            MessageQueue<OutgoingMessage> outgoingMessageQueue,
                            Store store,
                            SubscriptionRegistry subscriptionRegistry) {

        this.incomingMessageQueue = incomingMessageQueue;
        this.outgoingMessageQueue = outgoingMessageQueue;
        this.store = store;
        this.subscriptionRegistry = subscriptionRegistry;
    }

    private void process(IncomingMessage incomingMessage) {
        LOGGER.trace("Process client request. message=<{}>", incomingMessage);
        JsonNode node;
        try {
            node = OBJECT_MAPPER.readTree(incomingMessage.content());
        } catch (JsonProcessingException e) {
            LOGGER.warn("Processing event threw an exception. message=<{}>", incomingMessage);
            LOGGER.warn("Error during deserialization of event.", e);
            throw new RuntimeException("error during deserialization", e);
        }

        String requestType = node.get(0).asText();
        switch (requestType) {
            case "EVENT" -> {
                try {
                    var eventNode = node.get(1);
                    var event = OBJECT_MAPPER.treeToValue(eventNode, Event.class);
                    String eventString = eventNode.toString();
                    store.store(eventString);

                    // TODO fan out event to those who are subscribed?
                    var subscriptions = subscriptionRegistry.getAllSubscriptions();
                    LOGGER.debug("All subscriptions: <{}>", subscriptions);
                    subscription:
                    for (var subscription : subscriptions.entrySet()) {
                        LOGGER.debug("Processing subscription: <{}>", subscription);
                        var subscriptionId = subscription.getKey();
                        for (var filter : subscription.getValue()) {
                            if (!filter.evaluate(event)) {
                                continue subscription;
                            }
                        }

                        var owner = subscriptionRegistry.getOwner(subscriptionId);
                        LOGGER.debug("subscription found. sending to outgoing message queue. owner=<{}> subscriptionId=<{}> event=<{}>", owner, subscriptionId, eventString);
                        outgoingMessageQueue.add(
                                new OutgoingMessage(
                                        owner,
                                        OutgoingMessage.Type.EVENT,
                                        subscriptionId,
                                        eventString
                                )
                        );
                    }

                } catch (JsonProcessingException e) {
                    LOGGER.warn("Processing event threw an exception. node=<{}>", node);
                    LOGGER.warn("Error during deserialization of event node.", e);
                    throw new RuntimeException("Error during deserialization of event.", e);
                }
            }
            case "REQ" -> {
                String subscriptionId = node.get(1).asText();
                List<Filters> filters = new ArrayList<>();
                for (int i = 2; i < node.size(); i++) {
                    try {
                        filters.add(OBJECT_MAPPER.treeToValue(node.get(i), Filters.class));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("error during deserializing event.", e);
                    }
                }
                subscriptionRegistry.addSubscription(incomingMessage.owner(), subscriptionId, filters);

                // FIXME asynchronously respond via a different mechanism? this thread will block until the store returns results.
                List<String> events = store.find(filters);
                if (!events.isEmpty()) {
                    for (String event : events) {
                        outgoingMessageQueue.add(
                                new OutgoingMessage(
                                        incomingMessage.owner(),
                                        OutgoingMessage.Type.EVENT,
                                        subscriptionId,
                                        event
                                )
                        );
                    }
                }
            }
            case "CLOSE" -> {
                String subscriptionId = node.get(1).asText();
                subscriptionRegistry.removeSubscription(incomingMessage.owner(), subscriptionId);
                outgoingMessageQueue.add(
                        new OutgoingMessage(
                                incomingMessage.owner(),
                                OutgoingMessage.Type.CLOSE,
                                subscriptionId,
                                null
                        )
                );
            }
            default -> throw new RuntimeException("Unknown message type: " + requestType);
        }
    }

    @Override
    public void start() throws Exception {
        es.scheduleAtFixedRate(() -> {
            while (!incomingMessageQueue.isEmpty()) {
                IncomingMessage incomingMessage = incomingMessageQueue.remove();
                processingPool.submit(() -> process(incomingMessage));
            }

        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() throws Exception {
        es.shutdown();
        processingPool.shutdown();
    }
}
