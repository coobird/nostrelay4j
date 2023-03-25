package net.coobird.nostr.relay.messaging;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
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
    private final ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("out-message-scheduled-thread-%d")
                    .setUncaughtExceptionHandler((t, e) -> {
                        LOGGER.error("Uncaught exception t=<{}> e=<{}>", t, e);
                    })
                    .build()
    );

    public OutgoingMessageProcessor(MessageQueue<OutgoingMessage> outgoingMessageQueue,
                                    Set<MessageConsumer<OutgoingMessage>> outgoingMessageConsumers) {
        this.outgoingMessageQueue = outgoingMessageQueue;
        this.outgoingMessageConsumers = outgoingMessageConsumers;
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
