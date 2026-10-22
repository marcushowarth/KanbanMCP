package eu.howarth.mcp.kanban;

import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpStreamableTestClient;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.MultiMap;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Through the real MCP {@code tools/call} path, against a stub Kanboard: which
 * account each endpoint's calls reach Kanboard as. This is what proves the OAuth
 * identity is visible on the thread that runs the tool.
 */
@QuarkusTest
@QuarkusTestResource(StubKanboard.class)
class PerUserCredentialsMcpTest {

    @Inject
    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String authServerUrl;

    @BeforeEach
    void reset() {
        StubKanboard.callers.clear();
        StubKanboard.bodies.clear();
    }

    private String tokenFor(String username, String password) {
        return given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "kanban-mcp-test-client")
                .formParam("username", username)
                .formParam("password", password)
                .when().post(authServerUrl + "/protocol/openid-connect/token")
                .then().statusCode(200)
                .extract().path("access_token");
    }

    private static void callGetAllProjects(McpStreamableTestClient client, boolean expectError) {
        client.when()
                .toolsCall("getAllProjects", Map.of(),
                        response -> assertEquals(expectError, response.isError()))
                .thenAssertResults();
        client.disconnect();
    }

    @Test
    void oauthUserReachesKanboardAsTheirMappedAccount() {
        String token = tokenFor("test-user", "test-password");
        McpStreamableTestClient client = McpAssured.newStreamableClient()
                .setMcpPath("/oauth/mcp")
                .setAdditionalHeaders(msg -> MultiMap.caseInsensitiveMultiMap()
                        .add("Authorization", "Bearer " + token))
                .build()
                .connect();

        callGetAllProjects(client, false);

        assertEquals(List.of("test-kanboard-user"), StubKanboard.callers);
    }

    @Test
    void unmappedOAuthUserIsRefusedAndNeverReachesKanboard() {
        String token = tokenFor("unmapped-user", "unmapped-password");
        McpStreamableTestClient client = McpAssured.newStreamableClient()
                .setMcpPath("/oauth/mcp")
                .setAdditionalHeaders(msg -> MultiMap.caseInsensitiveMultiMap()
                        .add("Authorization", "Bearer " + token))
                .build()
                .connect();

        callGetAllProjects(client, true);

        assertTrue(StubKanboard.callers.isEmpty());
    }

    @Test
    void bearerPathWithAStaticTokenUsesTheDefaultAccount() {
        McpStreamableTestClient client = McpAssured.newStreamableClient()
                .setAdditionalHeaders(msg -> MultiMap.caseInsensitiveMultiMap()
                        .add("Authorization", "Bearer some-static-non-jwt-token"))
                .build()
                .connect();

        callGetAllProjects(client, false);

        assertEquals(List.of("claude"), StubKanboard.callers);
    }

    @Test
    void commentsAreAuthoredByTheCallingAccount() {
        String token = tokenFor("test-user", "test-password");
        McpStreamableTestClient client = McpAssured.newStreamableClient()
                .setMcpPath("/oauth/mcp")
                .setAdditionalHeaders(msg -> MultiMap.caseInsensitiveMultiMap()
                        .add("Authorization", "Bearer " + token))
                .build()
                .connect();

        client.when()
                .toolsCall("createComment", Map.of("taskId", 1, "content", "hello"),
                        response -> assertFalse(response.isError()))
                .toolsCall("createComment", Map.of("taskId", 1, "content", "again"),
                        response -> assertFalse(response.isError()))
                .thenAssertResults();
        client.disconnect();

        List<String> comments = StubKanboard.bodies.stream().filter(b -> b.contains("\"createComment\"")).toList();
        assertEquals(2, comments.size());
        comments.forEach(body -> assertTrue(body.contains("\"user_id\":42"), body));
        assertEquals(1, StubKanboard.bodies.stream().filter(b -> b.contains("\"getMe\"")).count(),
                "getMe is looked up once per account, then cached");
    }
}
