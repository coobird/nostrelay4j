package net.coobird.nostr.relay;

import net.coobird.nostr.relay.config.ConfigurationManager;
import net.coobird.nostr.relay.messaging.IncomingMessage;
import net.coobird.nostr.relay.messaging.MessageConsumer;
import net.coobird.nostr.relay.messaging.IncomingMessageProcessor;
import net.coobird.nostr.relay.messaging.MessageProducer;
import net.coobird.nostr.relay.messaging.MessageQueue;
import net.coobird.nostr.relay.messaging.OutgoingMessage;
import net.coobird.nostr.relay.messaging.OutgoingMessageProcessor;
import net.coobird.nostr.relay.server.ApplicationContext;
import net.coobird.nostr.relay.server.AdminServer;
import net.coobird.nostr.relay.server.MainServer;
import net.coobird.nostr.relay.server.websocket.LifecycleCallback;
import net.coobird.nostr.relay.store.StoreSelector;
import net.coobird.nostr.relay.subscription.DefaultSubscriptionRegistry;
import net.coobird.nostr.relay.subscription.Owner;
import net.coobird.nostr.relay.subscription.SubscriptionMonitor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String WELCOME_MESSAGE = """
             ________________________
            < Welcome to nostrelay4j >
             ------------------------
                    \\   ^__^
                     \\  (oo)\\_______
                        (__)\\       )\\/\\
                            ||----w |
                            ||     ||
            """;
    private static final String BANNER = """
                                 __            __            __ __  _\s
               ____  ____  _____/ /_________  / /___ ___  __/ // / (_)
              / __ \\/ __ \\/ ___/ __/ ___/ _ \\/ / __ `/ / / / // /_/ /\s
             / / / / /_/ (__  ) /_/ /  /  __/ / /_/ / /_/ /__  __/ / \s
            /_/ /_/\\____/____/\\__/_/   \\___/_/\\__,_/\\__, /  /_/_/ /  \s
                                                   /____/    /___/   \s
            """;

    public static void main(String[] args) throws Exception {
        WELCOME_MESSAGE.lines().forEach(LOGGER::info);

        var config = ConfigurationManager.getConfigurations();
        LOGGER.info("Using configuration: <{}>", config);

        final var subscriptionRegistry = new DefaultSubscriptionRegistry();
        final var store = StoreSelector.getStore();

        MessageQueue<IncomingMessage> incomingMessageQueue = new MessageQueue<>();
        MessageQueue<OutgoingMessage> outgoingMessageQueue = new MessageQueue<>();
        Set<MessageConsumer<OutgoingMessage>> outgoingMessageConsumers = new HashSet<>();

        LifecycleCallback lifecycleCallback = new LifecycleCallback() {
            @Override
            public void onRegister(Session session, MessageConsumer<OutgoingMessage> outgoingMessageConsumer) {
                outgoingMessageConsumers.add(outgoingMessageConsumer);
            }

            @Override
            public void onUnregister(Session session, MessageConsumer<OutgoingMessage> outgoingMessageConsumer) {
                subscriptionRegistry.unsubscribeAllForOwner(new Owner(outgoingMessageConsumer));
                outgoingMessageConsumers.remove(outgoingMessageConsumer);
            }
        };
        MessageProducer<IncomingMessage> incomingMessageProducer = new MessageProducer<IncomingMessage>() {
            @Override
            public void send(IncomingMessage message) {
                incomingMessageQueue.add(message);
            }
        };

        ApplicationContext applicationContext = new ApplicationContext();

        IncomingMessageProcessor incomingMessageProcessor = new IncomingMessageProcessor(
                incomingMessageQueue,
                outgoingMessageQueue,
                store,
                subscriptionRegistry
        );
        incomingMessageProcessor.start();
        applicationContext.add(incomingMessageProcessor);

        OutgoingMessageProcessor outgoingMessageProcessor = new OutgoingMessageProcessor(
                outgoingMessageQueue,
                outgoingMessageConsumers
        );
        outgoingMessageProcessor.start();
        applicationContext.add(outgoingMessageProcessor);

        final var subscriptionMonitor = new SubscriptionMonitor(subscriptionRegistry);
        subscriptionMonitor.start();
        applicationContext.add(subscriptionMonitor);

        var mainServer = new MainServer(
                () -> new Server(config.getServer().getMainPort()),
                config,
                lifecycleCallback,
                incomingMessageProducer
        );
        mainServer.start();
        applicationContext.add(mainServer);

        var adminServer = new AdminServer(
                () -> new Server(config.getServer().getAdminPort()),
                applicationContext
        );
        adminServer.start();
        applicationContext.add(adminServer);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Thread.currentThread().setName("shutdown-hook-thread");
            LOGGER.info("Shutdown hook called. Attempting graceful shutdown.");
            for (var application : applicationContext.getAll()) {
                LOGGER.info("Attempt shutting down: <{}>", application);
                try {
                    application.stop();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }));
    }
}
