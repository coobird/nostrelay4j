package net.coobird.nostr.relay.server.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.coobird.nostr.relay.config.Configurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

public class Nip11InformationServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final String response;

    private static final List<Integer> SUPPORTED_NIPS = Arrays.asList(1, 11);
    private static final String REPOSITORY_URL = "https://github.com/coobird/nostrelay4j";
    private static final String VERSION = "0.0.0";
    private static final String DEFAULT_RESPONSE = """
            {
                "name": "",
                "description": "",
                "pubkey": "",
                "contact": "",
                "supported-nips": [1, 11],
                "software": "https://github.com/coobird/nostrelay4j",
                "version": "0.0.0"
            }
    """;

    public Nip11InformationServlet(Configurations config) {
        Response response = new Response(
                config.getServerInfo().getName(),
                config.getServerInfo().getDescription(),
                config.getServerInfo().getPubkey(),
                config.getServerInfo().getContact(),
                SUPPORTED_NIPS,
                REPOSITORY_URL,
                VERSION
        );

        String tmpResponse;
        var objectMapper = new ObjectMapper();
        try {
            tmpResponse = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Error during response serialization.", e);
            LOGGER.warn("Using default NIP-11 response.");
            tmpResponse = DEFAULT_RESPONSE;
        }
        this.response = tmpResponse;
    }

    @JsonPropertyOrder({
            "name", "description", "pubkey", "contact", "supportedNips", "software", "version"
    })
    private record Response(
            String name,
            String description,
            String pubkey,
            String contact,
            @JsonProperty("supported-nips") List<Integer> supportedNips,
            String software,
            String version
    ) {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if ("application/nostr+json".equals(req.getHeader("Accept"))) {
            resp.setHeader("Access-Control-Allow-Origin", "*");
            resp.setHeader("Access-Control-Allow-Headers", "*");
            resp.setHeader("Access-Control-Allow-Methods", "GET, POST");
            resp.getWriter().print(response);
        }
    }
}
