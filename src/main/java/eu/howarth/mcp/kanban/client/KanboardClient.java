package eu.howarth.mcp.kanban.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.howarth.mcp.kanban.config.KanboardProperties;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class KanboardClient {

    private static final Logger log = Logger.getLogger(KanboardClient.class);

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

            log.debugf("Kanboard request: %s %s", method, body);

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

            log.debugf("Kanboard response: %s", httpResponse.body());

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
        JsonNode result = execute(method, params);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception e) {
            throw new KanboardException("Failed to serialize response for: " + method, e);
        }
    }
}
