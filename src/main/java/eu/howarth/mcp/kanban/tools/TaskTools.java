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
public class TaskTools {

    private final KanboardClient client;

    public TaskTools(KanboardClient client) {
        this.client = client;
    }

    @Tool(description = "Search for tasks across all projects using a query string. Supports Kanboard search syntax (e.g., 'status:open assignee:me', project name, or free text).")
    public String searchTasks(
            @ToolArg(description = "The numeric ID of the project to search in") int projectId,
            @ToolArg(description = "Search query using Kanboard search syntax") String query) {
        return client.executePretty("searchTasks", Map.of(
                "project_id", projectId,
                "query", query
        ));
    }

    @Tool(description = "Get detailed information about a specific task by its ID, including title, description, assignee, due date, column, and all metadata.")
    public String getTask(
            @ToolArg(description = "The numeric ID of the task") int taskId) {
        return client.executePretty("getTask", Map.of("task_id", taskId));
    }

    @Tool(description = "Create a new task in a project. Only title and project_id are required; all other fields are optional.")
    public String createTask(
            @ToolArg(description = "Title of the new task") String title,
            @ToolArg(description = "The numeric ID of the project") int projectId,
            @ToolArg(description = "Task description in Markdown format (optional)", required = false) String description,
            @ToolArg(description = "Color identifier e.g. 'blue', 'green', 'red', 'yellow' (optional)", required = false) String colorId,
            @ToolArg(description = "The column ID to place the task in (optional, defaults to first column)", required = false) Integer columnId,
            @ToolArg(description = "The user ID to assign the task to (optional)", required = false) Integer ownerId,
            @ToolArg(description = "Due date in YYYY-MM-DD format (optional)", required = false) String dateDue,
            @ToolArg(description = "Category ID to classify the task (optional; get valid IDs from getAllCategories)", required = false) Integer categoryId) {
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        params.put("project_id", projectId);
        if (description != null) params.put("description", description);
        if (colorId != null) params.put("color_id", colorId);
        if (columnId != null) params.put("column_id", columnId);
        if (ownerId != null) params.put("owner_id", ownerId);
        if (dateDue != null) params.put("date_due", dateDue);
        if (categoryId != null) params.put("category_id", categoryId);
        return client.executePretty("createTask", params);
    }

    @Tool(description = "Update an existing task's fields (title, description, color, owner, due date, priority, category). Only provide the fields you want to change; unspecified fields remain unchanged. Does not move the task between columns or projects — use moveTaskPosition or moveTaskToProject for that.")
    public String updateTask(
            @ToolArg(description = "The numeric ID of the task to update") int taskId,
            @ToolArg(description = "New title (optional)", required = false) String title,
            @ToolArg(description = "New description in Markdown (optional)", required = false) String description,
            @ToolArg(description = "New color identifier (optional)", required = false) String colorId,
            @ToolArg(description = "New owner/assignee user ID (optional)", required = false) Integer ownerId,
            @ToolArg(description = "New due date in YYYY-MM-DD format (optional)", required = false) String dateDue,
            @ToolArg(description = "Priority value (optional)", required = false) Integer priority,
            @ToolArg(description = "New category ID to classify the task (optional; get valid IDs from getAllCategories)", required = false) Integer categoryId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", taskId);
        if (title != null) params.put("title", title);
        if (description != null) params.put("description", description);
        if (colorId != null) params.put("color_id", colorId);
        if (ownerId != null) params.put("owner_id", ownerId);
        if (dateDue != null) params.put("date_due", dateDue);
        if (priority != null) params.put("priority", priority);
        if (categoryId != null) params.put("category_id", categoryId);
        return client.executePretty("updateTask", params);
    }

    @Tool(description = "Close a task (mark it as done). The task will be marked as completed/inactive.")
    public String closeTask(
            @ToolArg(description = "The numeric ID of the task to close") int taskId) {
        return client.executePretty("closeTask", Map.of("task_id", taskId));
    }

    @Tool(description = "Reopen a previously closed task. The task will be marked as active again.")
    public String openTask(
            @ToolArg(description = "The numeric ID of the task to reopen") int taskId) {
        return client.executePretty("openTask", Map.of("task_id", taskId));
    }

    @Tool(description = "Move a task to a different project, optionally into a specific column and swimlane in the destination project.")
    public String moveTaskToProject(
            @ToolArg(description = "The numeric ID of the task to move") int taskId,
            @ToolArg(description = "The numeric ID of the destination project") int projectId,
            @ToolArg(description = "The column ID in the destination project (optional)", required = false) Integer columnId,
            @ToolArg(description = "The swimlane ID in the destination project (optional, defaults to 0)", required = false) Integer swimlaneId) {
        Map<String, Object> params = new HashMap<>();
        params.put("task_id", taskId);
        params.put("project_id", projectId);
        if (columnId != null) params.put("column_id", columnId);
        params.put("swimlane_id", swimlaneId != null ? swimlaneId : 0);
        return client.executePretty("moveTaskToProject", params);
    }
}
