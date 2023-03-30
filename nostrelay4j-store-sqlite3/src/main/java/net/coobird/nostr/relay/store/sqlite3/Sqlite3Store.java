package net.coobird.nostr.relay.store.sqlite3;

import net.coobird.nostr.relay.model.Event;
import net.coobird.nostr.relay.model.EventTag;
import net.coobird.nostr.relay.model.Filters;
import net.coobird.nostr.relay.model.PubkeyTag;
import net.coobird.nostr.relay.model.Tag;
import net.coobird.nostr.relay.store.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Sqlite3Store implements Store {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Connection connection;

    // TODO When instantiated, initialize DB if not available.

    private boolean createTable() throws SQLException {
        try (var statement = connection.createStatement()) {
            return statement.execute("""
                CREATE TABLE IF NOT EXISTS events_v1 (
                    id              TEXT NOT NULL,
                    pubkey          TEXT NOT NULL,
                    created_at      INTEGER NOT NULL,
                    kind            INTEGER NOT NULL,
                    tags_e          TEXT,
                    tags_p          TEXT,
                    content         TEXT NOT NULL,
                    sig             TEXT NOT NULL,
                    raw_string      TEXT NOT NULL
                )
            """);
        }
    }

    private static String INSERT_SQL = "INSERT INTO events_v1 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static String serializeETagList(List<Tag> tags) {
        return tags.stream()
                .filter(tag -> tag.getType().equals("e"))
                .map(tag -> String.format(":%s", ((EventTag) tag).eventId()))
                .collect(Collectors.joining(":"));
    }

    private static String serializePTagList(List<Tag> tags) {
        return tags.stream()
                .filter(tag -> tag.getType().equals("p"))
                .map(tag -> String.format(":%s", ((PubkeyTag) tag).key()))
                .collect(Collectors.joining(":"));
    }

    private void storeEvent(Event event, String rawEvent) {
        try (var statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setString(1, event.id());
            statement.setString(2, event.pubkey());
            statement.setLong(3, event.createdAt());
            statement.setInt(4, event.kind());
            statement.setString(5, serializeETagList(event.tags()));
            statement.setString(6, serializePTagList(event.tags()));
            statement.setString(7, event.content());
            statement.setString(8, event.sig());
            statement.setString(9, rawEvent);
            statement.execute();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Sqlite3Store(Sqlite3StoreConfigurations configuration) throws Exception {
        var dbPath = configuration.getDbPath();
        LOGGER.info("Using DB path: <{}>", dbPath);

        connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", dbPath));
        LOGGER.debug("Initialized connection: <{}>", connection);

        LOGGER.debug("Attempt to create table.");
        boolean isChanged = createTable();
        LOGGER.debug("Table initialized: <{}>", isChanged);
    }

    @Override
    public void store(Event event, String rawEvent) {
        LOGGER.debug("Storing event: <{}>", event);
        storeEvent(event, rawEvent);
        LOGGER.debug("Stored event: <{}>", event);
    }


    @Override
    public List<String> find(List<Filters> filters) {
        var statementAndValue = FindSqlGenerator.generateFindSql(filters);

        try (var statement = connection.prepareStatement(statementAndValue.preparedSql())) {
            var values = statementAndValue.values();
            int index = 1;
            for (var value : values) {
                if (value instanceof String) {
                    statement.setString(index, (String)value);
                }
                index++;
            }

            List<String> events = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                events.add(resultSet.getString(9));
            }

            return events;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
