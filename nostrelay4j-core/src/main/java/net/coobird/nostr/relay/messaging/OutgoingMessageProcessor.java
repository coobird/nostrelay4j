package net.coobird.nostr.relay.messaging;

import net.coobird.nostr.relay.server.Lifecycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OutgoingMessageProcessor implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final MessageQueue<OutgoingMessage> outgoingMessageQueue;
    private final Set<MessageConsumer<OutgoingMessage>> outgoingMessageConsumers;
    private final ScheduledExecutorService es;

    public OutgoingMessageProcessor(MessageQueue<OutgoingMessage> outgoingMessageQueue,
                                    Set<MessageConsumer<OutgoingMessage>> outgoingMessageConsumers) {
        this.outgoingMessageQueue = outgoingMessageQueue;
        this.outgoingMessageConsumers = outgoingMessageConsumers;
        this.es = Executors.newScheduledThreadPool(1);
    }

    @Override
    public void start() throws Exception {
        es.scheduleAtFixedRate(() -> {
            while (!outgoingMessageQueue.isEmpty()) {
                var message = outgoingMessageQueue.remove();
                LOGGER.debug("Got message from outgoing queue: <{}>", message);
                LOGGER.debug("outgoingMessageConsumers: <{}>", outgoingMessageConsumers);

                for (var outgoingMessageConsumer : outgoingMessageConsumers) {
                    LOGGER.debug("outgoingMessageConsumer: <{}>", outgoingMessageConsumer);
                    outgoingMessageConsumer.receive(message);
                }
            }
        }, 0, 10, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() throws Exception {
        es.shutdown();
    }
}
