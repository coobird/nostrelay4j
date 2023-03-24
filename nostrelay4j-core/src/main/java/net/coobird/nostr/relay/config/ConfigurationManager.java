package net.coobird.nostr.relay.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;

public class ConfigurationManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static InputStream getConfigurationAsStream() {
        // TODO replace with real configurations
        String configAsString = "{}";
        return new ByteArrayInputStream(configAsString.getBytes(StandardCharsets.UTF_8));
    }

    public static Configurations getConfigurations() {
        try (InputStream is = getConfigurationAsStream()) {
            return Configurations.getConfigurations(is);
        } catch (Exception e) {
            LOGGER.error("Exception thrown while reading configuration.", e);
            LOGGER.warn("Using default configuration instead.");
            return new Configurations();
        }
    }
}
