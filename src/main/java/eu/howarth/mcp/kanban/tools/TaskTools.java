package eu.howarth.mcp.kanban.tools;

import eu.howarth.mcp.kanban.client.KanboardClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TaskTools {

    private final KanboardClient client;

    public TaskTools(KanboardClient client) {
        this.client = client;
    }

    @Tool(description = "Search for tasks across all projects using a query string. Supports Kanboard search syntax (e.g., 'status:open assignee:me', project name, or free text).")
    public String searchTasks(
            @ToolParam(description = "The numeric ID of the project to search in") int projectId,
            @ToolParam(description = "Search query using Kanboard search syntax") String query) {
        return client.executePretty("searchTasks", Map.of(
                "project_id", projectId,
                "query", query
        ));
    }

    @Tool(description = "Get detailed information about a specific task by its ID, including title, description, assignee, due date, column, and all metadata.")
    public String getTask(
            @ToolParam(description = "The numeric ID of the task") int taskId) {
        return client.executePretty("getTask", Map.of("task_id", taskId));
    }

    @Tool(description = "Create a new task in a project. Only title and project_id are required; all other fields are optional.")
    public String createTask(
            @ToolParam(description = "Title of the new task") String title,
            @ToolParam(description = "The numeric ID of the project") int projectId,
            @ToolParam(description = "Task description in Markdown format (optional)", required = false) String description,
            @ToolParam(description = "Color identifier e.g. 'blue', 'green', 'red', 'yellow' (optional)", required = false) String colorId,
            @ToolParam(description = "The column ID to place the task in (optional, defaults to first column)", required = false) Integer columnId,
            @ToolParam(description = "The user ID to assign the task to (optional)", required = false) Integer ownerId,
            @ToolParam(description = "Due date in YYYY-MM-DD format (optional)", required = false) String dateDue) {
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        params.put("project_id", projectId);
        if (description != null) params.put("description", description);
        if (colorId != null) params.put("color_id", colorId);
        if (columnId != null) params.put("column_id", columnId);
        if (ownerId != null) params.put("owner_id", ownerId);
        if (dateDue != null) params.put("date_due", dateDue);
        return client.executePretty("createTask", params);
    }

    @Tool(description = "Update an existing task. Only provide the fields you want to change; unspecified fields remain unchanged.")
    public String updateTask(
            @ToolParam(description = "The numeric ID of the task to update") int taskId,
            @ToolParam(description = "New title (optional)", required = false) String title,
            @ToolParam(description = "New description in Markdown (optional)", required = false) String description,
            @ToolParam(description = "New color identifier (optional)", required = false) String colorId,
            @ToolParam(description = "New owner/assignee user ID (optional)", required = false) Integer ownerId,
            @ToolParam(description = "New due date in YYYY-MM-DD format (optional)", required = false) String dateDue,
            @ToolParam(description = "Priority value (optional)", required = false) Integer priority) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", taskId);
        if (title != null) params.put("title", title);
        if (description != null) params.put("description", description);
        if (colorId != null) params.put("color_id", colorId);
        if (ownerId != null) params.put("owner_id", ownerId);
        if (dateDue != null) params.put("date_due", dateDue);
        if (priority != null) params.put("priority", priority);
        return client.executePretty("updateTask", params);
    }

    @Tool(description = "Close a task (mark it as done). The task will be marked as completed/inactive.")
    public String closeTask(
            @ToolParam(description = "The numeric ID of the task to close") int taskId) {
        return client.executePretty("closeTask", Map.of("task_id", taskId));
    }

    @Tool(description = "Reopen a previously closed task. The task will be marked as active again.")
    public String openTask(
            @ToolParam(description = "The numeric ID of the task to reopen") int taskId) {
        return client.executePretty("openTask", Map.of("task_id", taskId));
    }
}
