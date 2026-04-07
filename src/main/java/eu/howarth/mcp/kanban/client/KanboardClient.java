package eu.howarth.mcp.kanban.client;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import eu.howarth.mcp.kanban.config.KanboardProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class KanboardClient {

    private static final Logger log = LoggerFactory.getLogger(KanboardClient.class);

    private final KanboardProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final AtomicInteger requestId = new AtomicInteger(1);

    public KanboardClient(KanboardProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public JsonNode execute(String method) {
        return execute(method, Map.of());
    }

    public JsonNode execute(String method, Map<String, Object> params) {
        try {
            var request = new JsonRpcRequest(method, requestId.getAndIncrement(), params);
            String body = objectMapper.writeValueAsString(request);

            log.debug("Kanboard request: {} {}", method, body);

            String credentials = properties.username() + ":" + properties.apiToken();
            String authHeader = "Basic " + Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(properties.url()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", authHeader)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(
                    httpRequest, HttpResponse.BodyHandlers.ofString());

            log.debug("Kanboard response: {}", httpResponse.body());

            JsonRpcResponse response = objectMapper.readValue(
                    httpResponse.body(), JsonRpcResponse.class);

            if (response.error() != null && !response.error().isNull()) {
                throw new KanboardException("Kanboard API error: " + response.error().toString());
            }

            return response.result();
        } catch (KanboardException e) {
            throw e;
        } catch (Exception e) {
            throw new KanboardException("Failed to call Kanboard API: " + method, e);
        }
    }

    public String executePretty(String method) {
        return executePretty(method, Map.of());
    }

    public String executePretty(String method, Map<String, Object> params) {
        try {
            JsonNode result = execute(method, params);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception e) {
            throw new KanboardException("Failed to format response for: " + method, e);
        }
    }
}
