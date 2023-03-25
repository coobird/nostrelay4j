package net.coobird.nostr.relay.server.websocket;

import net.coobird.nostr.relay.messaging.IncomingMessage;
import net.coobird.nostr.relay.messaging.MessageConsumer;
import net.coobird.nostr.relay.messaging.MessageProducer;
import net.coobird.nostr.relay.messaging.OutgoingMessage;
import net.coobird.nostr.relay.subscription.Owner;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class WebsocketHandler extends WebSocketAdapter implements MessageConsumer<OutgoingMessage> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final LifecycleCallback lifecycleCallback;
    private final MessageProducer<IncomingMessage> incomingMessageProducer;

    public WebsocketHandler(LifecycleCallback lifecycleCallback, MessageProducer<IncomingMessage> incomingMessageProducer) {
        this.lifecycleCallback = lifecycleCallback;
        this.incomingMessageProducer = incomingMessageProducer;
    }

    @Override
    public void receive(OutgoingMessage message) {
        LOGGER.debug("Received outgoing message: <{}>", message);
        var session = getSession();
        if (!session.isOpen()) {
            LOGGER.warn("Session already closed. <{}>", session);
            return;
        }

        var subscriptionId = message.subscriptionId();
        try {
            switch (message.type()) {
                case EVENT -> {
                    var response = String.format(
                            "[\"EVENT\",\"%s\",%s]\n",
                            subscriptionId, message.content()
                    );
                    LOGGER.debug("send event: <{}>", response);
                    session.getRemote().sendString(response);
                }
                case NOTICE -> {
                    var response = String.format(
                            "[\"NOTICE\",\"Something went wrong with event id = %s: %s\"]",
                            subscriptionId, message.content()
                    );
                    LOGGER.debug("send notice: <{}>", response);
                    session.getRemote().sendString(response);
                }
                case CLOSE -> {
                    LOGGER.debug("receieved CLOSE for <{}>, subid=<{}>", message.owner(), message.subscriptionId());
                }
                default -> {
                    LOGGER.error("This shouldn't happen.");
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception thrown during processing outgoing message.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onWebSocketConnect(Session session) {
        LOGGER.debug("New session started: <{}>", session);
        super.onWebSocketConnect(session);
        lifecycleCallback.onRegister(getSession(), this);
    }

    @Override
    public void onWebSocketText(String message) {
        super.onWebSocketText(message);
        LOGGER.debug("Message received: session=<{}> message=<{}>", getSession(), message);
        incomingMessageProducer.send(new IncomingMessage(new Owner(this), message));
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        LOGGER.debug("Session closed: session=<{}> statusCode=<{}> reason=<{}>", getSession(), statusCode, reason);
        lifecycleCallback.onUnregister(getSession(), this);
    }
}
