package net.coobird.nostr.relay.server.websocket;

import net.coobird.nostr.relay.messaging.MessageConsumer;
import net.coobird.nostr.relay.messaging.OutgoingMessage;
import org.eclipse.jetty.websocket.api.Session;

public interface LifecycleCallback {
    void onRegister(Session session, MessageConsumer<OutgoingMessage> messageConsumer);
    void onUnregister(Session session, MessageConsumer<OutgoingMessage> messageConsumer);
}
