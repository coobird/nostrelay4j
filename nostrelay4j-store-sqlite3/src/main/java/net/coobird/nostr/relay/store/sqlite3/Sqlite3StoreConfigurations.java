package net.coobird.nostr.relay.store.sqlite3;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class Sqlite3StoreConfigurations {
    private static final String SQLITE3_IDENTIFIER = Sqlite3StoreProvider.getIdentifier();

    @JsonProperty("db_path")
    private String dbPath = "nostrelay4j.sqlite3";

    public Sqlite3StoreConfigurations() {
        // Don't do anything -- basically default value for db path stays.
        // This will be necessary when deserializing.
    }

    public Sqlite3StoreConfigurations(String dbPath) {
        this.dbPath = dbPath;
    }

    public static Sqlite3StoreConfigurations getConfiguration(InputStream is) {
        var objectMapper = new ObjectMapper();
        try {
            var configNode = objectMapper.readTree(is);
            if (configNode.has("store")) {
                var storeNode = configNode.get("store");
                if (storeNode.has(SQLITE3_IDENTIFIER)) {
                    var sqlite3Node = storeNode.get(SQLITE3_IDENTIFIER);
                    return objectMapper.treeToValue(sqlite3Node, Sqlite3StoreConfigurations.class);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new Sqlite3StoreConfigurations();
    }

    public String getDbPath() {
        return dbPath;
    }

    @Override
    public String toString() {
        return "Sqlite3StoreConfigurations{" +
                "dbPath='" + dbPath + '\'' +
                '}';
    }
}
