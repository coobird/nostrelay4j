package net.coobird.nostr.relay.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.coobird.nostr.relay.config.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Selects a {@link Store} based on the configurations in the JSON path {@code store.<store_id>},
 * where {@code store_id} is the value returned by {@link StoreProvider#identifier()}.
 */
public class StoreSelector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Store getStore() {
        LOGGER.debug("Store selector started.");
        ServiceLoader<StoreProvider> serviceLoader = ServiceLoader.load(StoreProvider.class);
        Map<String, StoreProvider> storeProviderMap = serviceLoader.stream()
                .collect(Collectors.toMap(p -> p.get().identifier(), ServiceLoader.Provider::get));
        LOGGER.debug("Known providers: <{}>", storeProviderMap);

        // Pick providers based on name in configuration.
        try (InputStream is = ConfigurationManager.getConfigurationAsStream()) {
            var objectMapper = new ObjectMapper();
            var configNode = objectMapper.readTree(is);
            if (configNode.has("store")) {
                var storeNode = configNode.get("store");
                for (Map.Entry<String, StoreProvider> entry: storeProviderMap.entrySet()) {
                    if (storeNode.has(entry.getKey())) {
                        LOGGER.debug("Using store provider: <{}>", entry.getValue());
                        return entry.getValue().create();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LOGGER.warn("Store not configured. Using a naive store.");
        return new NaiveStore();
    }
}
