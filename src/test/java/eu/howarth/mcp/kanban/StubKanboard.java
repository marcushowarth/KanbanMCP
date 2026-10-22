package eu.howarth.mcp.kanban;

import com.sun.net.httpserver.HttpServer;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A stand-in Kanboard JSON-RPC endpoint that records which account each call
 * authenticated as (the Basic auth username) and answers every method with an
 * empty list. {@code getMe} answers with a user id per account, so comment
 * authorship can be checked too.
 */
public class StubKanboard implements QuarkusTestResourceLifecycleManager {

    public static final List<String> callers = new CopyOnWriteArrayList<>();
    public static final List<String> bodies = new CopyOnWriteArrayList<>();
    public static final Map<String, Integer> USER_IDS = Map.of("claude", 29, "test-kanboard-user", 42);

    private HttpServer server;

    @Override
    public Map<String, String> start() {
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        server.createContext("/jsonrpc.php", exchange -> {
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            String user = new String(Base64.getDecoder().decode(auth.substring("Basic ".length())),
                    StandardCharsets.UTF_8).split(":", 2)[0];
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            callers.add(user);
            bodies.add(body);
            String result = body.contains("\"getMe\"")
                    ? "{\"id\":" + USER_IDS.get(user) + ",\"username\":\"" + user + "\"}"
                    : "[]";
            byte[] response = ("{\"jsonrpc\":\"2.0\",\"result\":" + result + ",\"id\":1}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        return Map.of(
                "kanboard.url", "http://127.0.0.1:" + server.getAddress().getPort() + "/jsonrpc.php",
                "kanboard.users.test-user.username", "test-kanboard-user",
                "kanboard.users.test-user.api-token", "test-kanboard-token");
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}
