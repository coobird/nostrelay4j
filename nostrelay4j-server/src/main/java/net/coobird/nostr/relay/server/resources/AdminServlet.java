package net.coobird.nostr.relay.server.resources;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.coobird.nostr.relay.server.ApplicationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class AdminServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ApplicationContext applicationContext;

    public AdminServlet(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("/shutdown".equals(req.getRequestURI())) {
            // FIXME curl reports 'curl: (18) transfer closed with outstanding read data remaining'
            resp.getWriter().print("Shutting down relay...");
            resp.getWriter().flush();
            for (var application : applicationContext.getAll()) {
                try {
                    LOGGER.info("Shutting down: {}", application);
                    application.stop();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            resp.sendError(404);
        }
    }
}
