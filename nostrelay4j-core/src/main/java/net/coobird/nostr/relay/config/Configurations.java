package net.coobird.nostr.relay.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Configurations {
    @JsonProperty("server_info")
    private ServerInfo serverInfo = new ServerInfo();

    public static class ServerInfo {
        @JsonProperty
        private String name = "nostrelay4j";
        @JsonProperty
        private String description = "A newly created relay.\nCustomize with your info.";
        @JsonProperty
        private String pubkey = "";
        @JsonProperty
        private String contact = "";

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getPubkey() {
            return pubkey;
        }

        public String getContact() {
            return contact;
        }

        @Override
        public String toString() {
            return "Server{" +
                    "name='" + name + '\'' +
                    ", description='" + description + '\'' +
                    ", pubkey='" + pubkey + '\'' +
                    ", contact='" + contact + '\'' +
                    '}';
        }
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    @JsonProperty
    public Server server = new Server();

    public static class Server {
        @JsonProperty
        private int mainPort = 8080;

        @JsonProperty
        private int adminPort = 8081;

        @JsonProperty
        private long websocketTimeout = Duration.ofMinutes(10).toMillis();

        public int getMainPort() {
            return mainPort;
        }

        public int getAdminPort() {
            return adminPort;
        }

        public long getWebsocketTimeout() {
            return websocketTimeout;
        }

        @Override
        public String toString() {
            return "Server{" +
                    "mainPort=" + mainPort +
                    ", adminPort=" + adminPort +
                    ", websocketTimeout=" + websocketTimeout +
                    '}';
        }
    }

    public Server getServer() {
        return server;
    }

    @JsonProperty("event_processing")
    private EventProcessing eventProcessing = new EventProcessing();

    public static class EventProcessing {
        @JsonProperty
        private int consumerThreadCount = 4;

        @JsonProperty
        private int maxEventsPerTick = 100;

        public int getConsumerThreadCount() {
            return consumerThreadCount;
        }

        public int getMaxEventsPerTick() {
            return maxEventsPerTick;
        }

        @Override
        public String toString() {
            return "EventProcessing{" +
                    "consumerThreadCount=" + consumerThreadCount +
                    ", maxEventsPerTick=" + maxEventsPerTick +
                    '}';
        }
    }

    public EventProcessing getEventProcessing() {
        return eventProcessing;
    }

    public static Configurations getConfigurations(InputStream is) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(is, Configurations.class);
    }

    @Override
    public String toString() {
        return "Configurations{" +
                "serverInfo=" + serverInfo +
                ", server=" + server +
                ", eventProcessing=" + eventProcessing +
                '}';
    }
}
