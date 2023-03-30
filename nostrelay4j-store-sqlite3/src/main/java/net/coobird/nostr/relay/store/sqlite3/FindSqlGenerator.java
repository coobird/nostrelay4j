package net.coobird.nostr.relay.store.sqlite3;

import net.coobird.nostr.relay.model.Filters;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FindSqlGenerator {
    record StatementAndValues(String preparedSql, List<Object> values) {}

    public static StatementAndValues generateFindSql(List<Filters> filters) {
        var baseSql = "SELECT id, pubkey, created_at, kind, tags_e, tags_p, content, sig, raw_string FROM events_v1 ";

        record ConditionAndValue(String condition, Object value) {}

        // FIXME Limitation - only supports a single filter.
        var filter = filters.get(0);
        List<ConditionAndValue> conditions = new ArrayList<>();
        if (filter.ids() != null && !filter.ids().isEmpty()) {
            filter.ids().stream()
                    // TODO Use LIKE for those shorter than full length, but = for full length.
                    .map(id -> new ConditionAndValue(
                            "id LIKE ?", id + "%"
                    )).forEach(conditions::add);
        }

        String conditionString = conditions.stream()
                .map(ConditionAndValue::condition)
                .collect(Collectors.joining(" OR "));

        String sql = String.format("%s WHERE %s", baseSql, conditionString);

        var values = conditions.stream().map(ConditionAndValue::value).toList();

        return new StatementAndValues(sql, values);
    }
}
