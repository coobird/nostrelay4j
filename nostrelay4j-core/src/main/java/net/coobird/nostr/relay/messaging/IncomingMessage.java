package net.coobird.nostr.relay.messaging;

import net.coobird.nostr.relay.subscription.Owner;

public record IncomingMessage(Owner owner, String content) {
}
