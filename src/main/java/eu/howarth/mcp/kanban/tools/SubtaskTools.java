package eu.howarth.mcp.kanban.tools;

import eu.howarth.mcp.kanban.client.KanboardClient;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.McpServer;
import static io.quarkiverse.mcp.server.McpServer.DEFAULT;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@McpServer(DEFAULT)
@McpServer("oauth")
public class SubtaskTools {

    private final KanboardClient client;

    public SubtaskTools(KanboardClient client) {
        this.client = client;
    }

    @Tool(description = "Get all subtasks (checklist items) for a task.")
    public String getAllSubtasks(
            @ToolArg(description = "The numeric ID of the task") int taskId) {
        return client.executePretty("getAllSubtasks", Map.of("task_id", taskId));
    }

    @Tool(description = "Create a subtask (checklist item) on a task.")
    public String createSubtask(
            @ToolArg(description = "The numeric ID of the task") int taskId,
            @ToolArg(description = "Title of the subtask") String title) {
        return client.executePretty("createSubtask", Map.of("task_id", taskId, "title", title));
    }

    @Tool(description = "Update a subtask. Status: 0=todo, 1=in progress, 2=done.")
    public String updateSubtask(
            @ToolArg(description = "The numeric ID of the subtask") int subtaskId,
            @ToolArg(description = "The numeric ID of the parent task") int taskId,
            @ToolArg(description = "New title (optional, omit to keep current)", required = false) String title,
            @ToolArg(description = "New status: 0=todo, 1=in progress, 2=done (optional)", required = false) Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", subtaskId);
        params.put("task_id", taskId);
        if (title != null) params.put("title", title);
        if (status != null) params.put("status", status);
        return client.executePretty("updateSubtask", params);
    }

    @Tool(description = "Remove a subtask (checklist item) from a task.")
    public String removeSubtask(
            @ToolArg(description = "The numeric ID of the subtask") int subtaskId) {
        return client.executePretty("removeSubtask", Map.of("subtask_id", subtaskId));
    }
}
