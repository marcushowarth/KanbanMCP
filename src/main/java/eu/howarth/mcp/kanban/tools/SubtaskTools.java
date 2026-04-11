package eu.howarth.mcp.kanban.tools;

import eu.howarth.mcp.kanban.client.KanboardClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SubtaskTools {

    private final KanboardClient client;

    public SubtaskTools(KanboardClient client) {
        this.client = client;
    }

    @Tool(description = "Get all subtasks (checklist items) for a task.")
    public String getAllSubtasks(
            @ToolParam(description = "The numeric ID of the task") int taskId) {
        return client.executePretty("getAllSubtasks", Map.of("task_id", taskId));
    }

    @Tool(description = "Create a subtask (checklist item) on a task.")
    public String createSubtask(
            @ToolParam(description = "The numeric ID of the task") int taskId,
            @ToolParam(description = "Title of the subtask") String title) {
        return client.executePretty("createSubtask", Map.of("task_id", taskId, "title", title));
    }

    @Tool(description = "Update a subtask. Status: 0=todo, 1=in progress, 2=done.")
    public String updateSubtask(
            @ToolParam(description = "The numeric ID of the subtask") int subtaskId,
            @ToolParam(description = "The numeric ID of the parent task") int taskId,
            @ToolParam(description = "New title (optional, omit to keep current)", required = false) String title,
            @ToolParam(description = "New status: 0=todo, 1=in progress, 2=done (optional)", required = false) Integer status) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", subtaskId);
        params.put("task_id", taskId);
        if (title != null) params.put("title", title);
        if (status != null) params.put("status", status);
        return client.executePretty("updateSubtask", params);
    }

    @Tool(description = "Remove a subtask (checklist item) from a task.")
    public String removeSubtask(
            @ToolParam(description = "The numeric ID of the subtask") int subtaskId) {
        return client.executePretty("removeSubtask", Map.of("id", subtaskId));
    }
}
