package eu.howarth.mcp.kanban.tools;

import eu.howarth.mcp.kanban.client.KanboardClient;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the swimlane contract of {@link BoardTools#moveTaskPosition} so we stop
 * re-deriving it by hand. The Kanboard RPC requires swimlane_id to be present
 * and treats 0 as "leave the swimlane as-is" (verified against the live board
 * 2026-06-16); omitting the key entirely errors with "Wrong number of arguments".
 * Hence the optional MCP arg must resolve to 0 when not supplied.
 */
class BoardToolsTest {

    /** Captures the params destined for Kanboard without hitting the network. */
    static class CapturingClient extends KanboardClient {
        Map<String, Object> lastParams;

        CapturingClient() {
            super(null, null);
        }

        @Override
        public String executePretty(String method, Map<String, Object> params) {
            this.lastParams = params;
            return "{}";
        }
    }

    @Test
    void omittedSwimlaneDefaultsToZero() {
        var client = new CapturingClient();
        new BoardTools(client).moveTaskPosition(7, 123, 33, 1, null);
        assertEquals(0, client.lastParams.get("swimlane_id"));
    }

    @Test
    void explicitSwimlaneIsPassedThrough() {
        var client = new CapturingClient();
        new BoardTools(client).moveTaskPosition(7, 123, 33, 1, 36);
        assertEquals(36, client.lastParams.get("swimlane_id"));
    }
}
