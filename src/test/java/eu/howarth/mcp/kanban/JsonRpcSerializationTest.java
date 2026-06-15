package eu.howarth.mcp.kanban;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.howarth.mcp.kanban.client.JsonRpcRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Plain Jackson test (no Quarkus boot needed): guards that JsonRpcRequest
 * serialises its params map keys/values verbatim, so the Kanboard JSON-RPC call
 * carries e.g. subtask_id rather than dropping it.
 */
class JsonRpcSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void removeSubtask_paramsContainsSubtaskId() throws Exception {
        var request = new JsonRpcRequest("removeSubtask", 1, Map.of("subtask_id", 308));
        String json = objectMapper.writeValueAsString(request);
        assertTrue(json.contains("\"subtask_id\""), "params must contain subtask_id key, got: " + json);
        assertTrue(json.contains("308"), "params must contain value 308, got: " + json);
    }

    @Test
    void getAllSubtasks_paramsContainsTaskId() throws Exception {
        var request = new JsonRpcRequest("getAllSubtasks", 1, Map.of("task_id", 868));
        String json = objectMapper.writeValueAsString(request);
        assertTrue(json.contains("\"task_id\""), "params must contain task_id key, got: " + json);
        assertTrue(json.contains("868"), "params must contain value 868, got: " + json);
    }

    @Test
    void removeSubtask_zeroValue_stillPresent() throws Exception {
        // If the MCP framework fails to map the argument, subtaskId defaults to 0
        var request = new JsonRpcRequest("removeSubtask", 1, Map.of("subtask_id", 0));
        String json = objectMapper.writeValueAsString(request);
        assertTrue(json.contains("\"subtask_id\""), "key must be present even for value=0, got: " + json);
    }
}
