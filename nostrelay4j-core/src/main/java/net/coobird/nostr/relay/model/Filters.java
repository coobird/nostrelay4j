package net.coobird.nostr.relay.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

public record Filters(
        List<String> ids,
        List<String> authors,
        List<Integer> kinds,
        @JsonProperty("#e") List<String> hashE,
        @JsonProperty("#p") List<String> hashP,
        Long since,
        Long until,
        Long limit
) {
    public boolean evaluate(Event event) {
        ids:
        if (ids != null) {
            var eventId = event.id();
            for (var id : ids) {
                if (id.isEmpty()) {
                    continue;
                }
                if (eventId.startsWith(id)) {
                    break ids;
                }
            }
            return false;
        }

        authors:
        if (authors != null) {
            var eventAuthor = event.pubkey();
            for (var author : authors) {
                if (author.isEmpty()) {
                    continue;
                }
                if (eventAuthor.startsWith(author)) {
                    break authors;
                }
            }
            return false;
        }

        kinds:
        if (kinds != null) {
            var eventKind = event.kind();
            for (var kind : kinds) {
                if (eventKind == kind) {
                    break kinds;
                }
            }
            return false;
        }

        etags:
        if (hashE != null) {
            var eventIds = event.tags().stream()
                    .filter(tag -> tag.getType().equals("e"))
                    .map(tag -> ((EventTag)tag).eventId())
                    .collect(Collectors.toSet());

            if (eventIds.isEmpty()) {
                return false;
            }

            for (var e : hashE) {
                if (eventIds.contains(e)) {
                    break etags;
                }
            }
            return false;
        }

        ptags:
        if (hashP != null) {
            var pubkeys = event.tags().stream()
                    .filter(tag -> tag.getType().equals("p"))
                    .map(tag -> ((EventTag)tag).eventId())
                    .collect(Collectors.toSet());

            if (pubkeys.isEmpty()) {
                return false;
            }

            for (var p : hashP) {
                if (pubkeys.contains(p)) {
                    break ptags;
                }
            }
            return false;
        }

        if (since != null) {
            if (event.createdAt() > since) {
                return false;
            }
        }

        if (until != null) {
            if (event.createdAt() < until) {
                return false;
            }
        }

        return true;
    }
}
