package eu.howarth.mcp.kanban.tools;

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
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Framework-level proof, through the real MCP {@code tools/call} path, that omitting the
 * optional {@code swimlaneId} arg results in {@code swimlane_id=0} being sent to Kanboard:
 * quarkus-mcp-server delivers {@code null} for an omitted optional boxed arg, and the wrapper
 * maps {@code null -> 0} (Kanboard's "keep current lane" sentinel). Neither the framework's
 * optional-arg defaulting nor Kanboard's 0-semantics is documented upstream, hence this guard.
 * Complements {@link BoardToolsTest}, which pins the same mapping at the method level.
 */
@QuarkusTest
class MoveTaskPositionMcpTest {

    @InjectMock
    KanboardClient kanboardClient;

    @Test
    void omittedSwimlaneArrivesAsZeroOverMcp() {
        when(kanboardClient.executePretty(eq("moveTaskPosition"), anyMap())).thenReturn("{}");

        McpStreamableTestClient client = McpAssured.newConnectedStreamableClient();
        client.when()
                .toolsCall("moveTaskPosition", Map.of(
                        "projectId", 7,
                        "taskId", 123,
                        "columnId", 33,
                        "position", 1), // swimlaneId deliberately omitted
                        response -> assertFalse(response.isError()))
                .thenAssertResults();
        client.disconnect();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> params = ArgumentCaptor.forClass(Map.class);
        verify(kanboardClient).executePretty(eq("moveTaskPosition"), params.capture());
        assertEquals(0, params.getValue().get("swimlane_id"));
    }
}
