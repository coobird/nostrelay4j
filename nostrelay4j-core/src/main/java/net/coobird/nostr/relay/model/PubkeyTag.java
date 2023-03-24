package net.coobird.nostr.relay.model;

public record PubkeyTag(String key, String recommendedRelayUrl) implements Tag {
    public PubkeyTag(String key) {
        this(key, "");
    }

    @Override
    public String getType() {
        return "p";
    }
}
