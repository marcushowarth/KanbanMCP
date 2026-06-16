package eu.howarth.mcp.kanban;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Native-image check on how the MCP tool schema reflects Java parameter kinds:
 * primitive args (e.g. {@code int}) are advertised as REQUIRED, while a boxed
 * {@code @ToolArg(required=false)} arg ({@code Integer swimlaneId}) is OPTIONAL.
 * Runs a real {@code tools/list} against the compiled artifact over Streamable
 * HTTP, so it also proves the tool-schema reflection survived the native build —
 * something the health IT (boot only) does not cover.
 *
 * Validated in JVM mode via {@code mvnw verify -DskipITs=false}; runs in native
 * under the 'native' profile ({@code mvnw verify -Dnative}).
 */
@QuarkusIntegrationTest
class KanbanMcpOptionalParamsIT {

    private static final String MCP = "/kanban/mcp";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void primitiveArgsRequired_boxedOptionalIsNot() throws Exception {
        RestAssured.basePath = "";

        Response init = mcpPost(null,
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{"
                        + "\"protocolVersion\":\"2025-06-18\",\"capabilities\":{},"
                        + "\"clientInfo\":{\"name\":\"params-it\",\"version\":\"1\"}}}");
        String session = init.getHeader("mcp-session-id");
        assertNotNull(session, "server must return an Mcp-Session-Id on initialize");

        mcpPost(session, "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");

        Response list = mcpPost(session, "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\"}");
        JsonNode schema = inputSchemaOf(list, "moveTaskPosition");

        List<String> required = new ArrayList<>();
        schema.path("required").forEach(n -> required.add(n.asText()));

        assertTrue(required.contains("projectId"), "primitive projectId should be required");
        assertTrue(required.contains("taskId"), "primitive taskId should be required");
        assertTrue(required.contains("columnId"), "primitive columnId should be required");
        assertTrue(required.contains("position"), "primitive position should be required");
        assertFalse(required.contains("swimlaneId"), "boxed optional swimlaneId should NOT be required");
    }

    private static Response mcpPost(String session, String body) {
        var req = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream");
        if (session != null) {
            req = req.header("Mcp-Session-Id", session);
        }
        return req.body(body).when().post(MCP);
    }

    /** Streamable HTTP may answer as plain JSON or as an SSE {@code data:} frame — handle both. */
    private static JsonNode inputSchemaOf(Response resp, String toolName) throws Exception {
        String raw = resp.getBody().asString();
        String json = raw.contains("data:")
                ? raw.lines()
                    .filter(l -> l.startsWith("data:"))
                    .map(l -> l.substring("data:".length()).trim())
                    .reduce((a, b) -> b)
                    .orElse(raw)
                : raw;
        JsonNode root = MAPPER.readTree(json);
        for (JsonNode tool : root.path("result").path("tools")) {
            if (toolName.equals(tool.path("name").asText())) {
                return tool.path("inputSchema");
            }
        }
        throw new AssertionError("tool not found in tools/list: " + toolName);
    }
}
