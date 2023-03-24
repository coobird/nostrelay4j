package net.coobird.nostr.relay.model;

public record EventTag(String eventId, String recommendedRelayUrl) implements Tag {
    public EventTag(String eventId) {
        this(eventId, "");
    }

    @Override
    public String getType() {
        return "e";
    }
}


