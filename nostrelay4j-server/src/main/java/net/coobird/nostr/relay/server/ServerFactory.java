package net.coobird.nostr.relay.server;

import org.eclipse.jetty.server.Server;

@FunctionalInterface
public interface ServerFactory {
    Server getInstance();
}
