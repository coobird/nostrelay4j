package net.coobird.nostr.relay.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ApplicationContext {
    private final List<Lifecycle> lifecycleObjects = new ArrayList<>();

    public void add(Lifecycle lifecycle) {
        lifecycleObjects.add(lifecycle);
    }

    public List<Lifecycle> getAll() {
        return Collections.unmodifiableList(lifecycleObjects);
    }
}
