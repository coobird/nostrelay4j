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
        boolean hasMatch = false;

        ids:
        if (ids != null && !ids.isEmpty()) {
            var eventId = event.id();
            for (var id : ids) {
                if (id.isEmpty()) {
                    continue;
                }
                if (eventId.startsWith(id)) {
                    hasMatch = true;
                    break ids;
                }
            }
            return false;
        }

        authors:
        if (authors != null && !authors().isEmpty()) {
            var eventAuthor = event.pubkey();
            for (var author : authors) {
                if (author.isEmpty()) {
                    continue;
                }
                if (eventAuthor.startsWith(author)) {
                    hasMatch = true;
                    break authors;
                }
            }
            return false;
        }

        kinds:
        if (kinds != null && !kinds.isEmpty()) {
            var eventKind = event.kind();
            for (var kind : kinds) {
                if (eventKind == kind) {
                    hasMatch = true;
                    break kinds;
                }
            }
            return false;
        }

        etags:
        if (hashE != null && !hashE.isEmpty()) {
            var eventIds = event.tags().stream()
                    .filter(tag -> tag.getType().equals("e"))
                    .map(tag -> ((EventTag)tag).eventId())
                    .collect(Collectors.toSet());

            if (eventIds.isEmpty()) {
                return false;
            }

            for (var e : hashE) {
                if (eventIds.contains(e)) {
                    hasMatch = true;
                    break etags;
                }
            }
            return false;
        }

        ptags:
        if (hashP != null && !hashP.isEmpty()) {
            var pubkeys = event.tags().stream()
                    .filter(tag -> tag.getType().equals("p"))
                    .map(tag -> ((EventTag)tag).eventId())
                    .collect(Collectors.toSet());

            if (pubkeys.isEmpty()) {
                return false;
            }

            for (var p : hashP) {
                if (pubkeys.contains(p)) {
                    hasMatch = true;
                    break ptags;
                }
            }
            return false;
        }

        if (since != null) {
            if (event.createdAt() > since) {
                return false;
            }
            hasMatch = true;
        }

        if (until != null) {
            if (event.createdAt() < until) {
                return false;
            }
            hasMatch = true;
        }

        return hasMatch;
    }
}
