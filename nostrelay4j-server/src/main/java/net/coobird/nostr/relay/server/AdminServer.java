package net.coobird.nostr.relay.server;

import net.coobird.nostr.relay.server.resources.AdminServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class AdminServer implements Lifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Server server;

    public AdminServer(ServerFactory serverFactory, ApplicationContext applicationContext) {
        server = serverFactory.getInstance();
        ServletContextHandler adminHandler = new ServletContextHandler(server, "/");
        adminHandler.addServlet(new ServletHolder(new AdminServlet(applicationContext)), "/");
        server.setHandler(adminHandler);
    }

    @Override
    public void start() throws Exception {
        server.start();
        LOGGER.info("Started admin server on port {}", server.getURI());
    }

    @Override
    public void stop() throws Exception {
        server.stop();
        LOGGER.info("Stopped admin server.");
    }
}
