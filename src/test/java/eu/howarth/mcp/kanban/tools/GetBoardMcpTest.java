package eu.howarth.mcp.kanban.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.howarth.mcp.kanban.client.KanboardClient;
import io.quarkiverse.mcp.server.test.McpAssured;
import io.quarkiverse.mcp.server.test.McpAssured.McpStreamableTestClient;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Through the real MCP {@code tools/call} path: a caller that sends only {@code projectId}
 * (as every caller did before the optional args existed) gets open cards as summaries.
 * Complements {@link GetBoardTest}, which pins the same behaviour at the method level.
 */
@QuarkusTest
class GetBoardMcpTest {

    @InjectMock
    KanboardClient kanboardClient;

    @Test
    void projectIdAloneGivesOpenCardSummaries() throws Exception {
        JsonNode board = new ObjectMapper().readTree("""
                [ { "id" : 9, "columns" : [ { "id" : 34, "tasks" : [
                  { "id" : 1, "title" : "Open", "description" : "text", "is_active" : 1, "tags" : [ ] },
                  { "id" : 2, "title" : "Closed", "description" : "text", "is_active" : 0, "tags" : [ ] }
                ] } ] } ]
                """);
        when(kanboardClient.execute(eq("getBoard"), anyMap())).thenReturn(board);
        when(kanboardClient.pretty(any())).thenReturn("{}");

        McpStreamableTestClient client = McpAssured.newConnectedStreamableClient();
        client.when()
                .toolsCall("getBoard", Map.of("projectId", 7), // includeClosed and fullDetail omitted
                        response -> assertFalse(response.isError()))
                .thenAssertResults();
        client.disconnect();

        ArgumentCaptor<JsonNode> sent = ArgumentCaptor.forClass(JsonNode.class);
        verify(kanboardClient).pretty(sent.capture());
        JsonNode cards = sent.getValue().get(0).get("columns").get(0).get("tasks");
        assertEquals(1, cards.size());
        assertEquals(1, cards.get(0).get("id").asInt());
        assertFalse(cards.get(0).has("description"));
    }
}
