package net.coobird.nostr.relay.server;

import net.coobird.nostr.relay.config.Configurations;
import net.coobird.nostr.relay.messaging.IncomingMessage;
import net.coobird.nostr.relay.messaging.MessageProducer;
import net.coobird.nostr.relay.server.websocket.LifecycleCallback;
import net.coobird.nostr.relay.server.resources.Nip11InformationServlet;
import net.coobird.nostr.relay.server.websocket.WebsocketHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.JettyWebSocketCreator;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.time.Duration;

public class MainServer implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Server server;
    private static final int MAX_SIZE = 128 * 1024;

    public MainServer(
            ServerFactory serverFactory,
            Configurations config,
            LifecycleCallback lifecycleCallback,
            MessageProducer<IncomingMessage> incomingMessageProducer
    ) {
        server = serverFactory.getInstance();
        ServletContextHandler handler = new ServletContextHandler(server, "/");
        handler.addServlet(new ServletHolder(new Nip11InformationServlet(config)), "/");
        server.setHandler(handler);

        final JettyWebSocketCreator webSocketCreator = (req, resp) -> new WebsocketHandler(lifecycleCallback, incomingMessageProducer);
        JettyWebSocketServletContainerInitializer.configure(handler, (servletContext, container) -> {
            container.setMaxTextMessageSize(MAX_SIZE);
            container.setMaxBinaryMessageSize(MAX_SIZE);
            container.setIdleTimeout(Duration.ofMillis(config.getServer().getWebsocketTimeout()));
            container.addMapping("/", webSocketCreator);
        });
    }

    @Override
    public void start() throws Exception {
        server.start();
        LOGGER.info("Started main server on port {}", server.getURI());
    }

    @Override
    public void stop() throws Exception {
        server.stop();
        LOGGER.info("Stopped main server.");
    }
}
