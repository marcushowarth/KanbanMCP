package eu.howarth.mcp.kanban;

import eu.howarth.mcp.kanban.client.JsonRpcRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JsonRpcSerializationTest {

    @Autowired
    private tools.jackson.databind.ObjectMapper objectMapper;

    @Test
    void removeSubtask_paramsContainsSubtaskId() throws Exception {
        var request = new JsonRpcRequest("removeSubtask", 1, Map.of("subtask_id", 308));
        String json = objectMapper.writeValueAsString(request);
        System.out.println("removeSubtask JSON: " + json);
        assertTrue(json.contains("\"subtask_id\""), "params must contain subtask_id key, got: " + json);
        assertTrue(json.contains("308"), "params must contain value 308, got: " + json);
    }

    @Test
    void getAllSubtasks_paramsContainsTaskId() throws Exception {
        var request = new JsonRpcRequest("getAllSubtasks", 1, Map.of("task_id", 868));
        String json = objectMapper.writeValueAsString(request);
        System.out.println("getAllSubtasks JSON: " + json);
        assertTrue(json.contains("\"task_id\""), "params must contain task_id key, got: " + json);
        assertTrue(json.contains("868"), "params must contain value 868, got: " + json);
    }
}
