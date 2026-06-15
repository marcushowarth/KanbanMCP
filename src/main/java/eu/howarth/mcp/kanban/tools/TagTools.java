package eu.howarth.mcp.kanban.tools;

import eu.howarth.mcp.kanban.client.KanboardClient;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class TagTools {

    private final KanboardClient client;

    public TagTools(KanboardClient client) {
        this.client = client;
    }

    @Tool(description = "Get all tags defined in a project.")
    public String getTagsByProject(
            @ToolArg(description = "The numeric ID of the project") int projectId) {
        return client.executePretty("getTagsByProject", Map.of("project_id", projectId));
    }

    @Tool(description = "Get all tags assigned to a task.")
    public String getTaskTags(
            @ToolArg(description = "The numeric ID of the task") int taskId) {
        return client.executePretty("getTaskTags", Map.of("task_id", taskId));
    }

    @Tool(description = "Set tags on a task, replacing any existing tags. Pass an empty list to remove all tags.")
    public String setTaskTags(
            @ToolArg(description = "The numeric ID of the project the task belongs to") int projectId,
            @ToolArg(description = "The numeric ID of the task") int taskId,
            @ToolArg(description = "List of tag names to assign to the task") List<String> tags) {
        return client.executePretty("setTaskTags", Map.of(
                "project_id", projectId,
                "task_id", taskId,
                "tags", tags
        ));
    }
}
