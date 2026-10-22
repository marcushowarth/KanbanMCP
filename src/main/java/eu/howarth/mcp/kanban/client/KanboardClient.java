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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class KanboardClient {

    private static final Logger log = Logger.getLogger(KanboardClient.class);

    private final KanboardProperties properties;
    private final KanboardCredentials credentials;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final AtomicInteger requestId = new AtomicInteger(1);
    private final Map<String, Integer> userIds = new ConcurrentHashMap<>();

    public KanboardClient(KanboardProperties properties, KanboardCredentials credentials, ObjectMapper objectMapper) {
        this.properties = properties;
        this.credentials = credentials;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
    }

    public JsonNode execute(String method) {
        return execute(method, Map.of());
    }

    public JsonNode execute(String method, Map<String, Object> params) {
        var caller = credentials.current();
        try {
            var request = new JsonRpcRequest(method, requestId.getAndIncrement(), params);
            String body = objectMapper.writeValueAsString(request);

            log.debugf("Kanboard request: %s %s", method, body);

            String authHeader = "Basic " + Base64.getEncoder()
                    .encodeToString((caller.username() + ":" + caller.apiToken()).getBytes(StandardCharsets.UTF_8));

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

    /** The calling account's Kanboard user id, from {@code getMe}, looked up once per account. */
    public int currentUserId() {
        return userIds.computeIfAbsent(credentials.current().username(),
                username -> execute("getMe").get("id").asInt());
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
