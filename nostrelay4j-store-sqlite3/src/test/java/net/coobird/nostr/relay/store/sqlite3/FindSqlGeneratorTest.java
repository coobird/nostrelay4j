package net.coobird.nostr.relay.store.sqlite3;

import net.coobird.nostr.relay.model.Filters;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FindSqlGeneratorTest {
    @Test
    public void foo() {
        List<Filters> filters = Collections.singletonList(
                new Filters(
                        Collections.singletonList("0000"),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        null,
                        null,
                        null
                )
        );

        System.out.println(FindSqlGenerator.generateFindSql(filters));
    }
}