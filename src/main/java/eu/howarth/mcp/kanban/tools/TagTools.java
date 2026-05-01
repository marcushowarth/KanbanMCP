package eu.howarth.mcp.kanban.tools;

import eu.howarth.mcp.kanban.client.KanboardClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TagTools {

    private final KanboardClient client;

    public TagTools(KanboardClient client) {
        this.client = client;
    }

    @Tool(description = "Get all tags defined in a project.")
    public String getTagsByProject(
            @ToolParam(description = "The numeric ID of the project") int projectId) {
        return client.executePretty("getTagsByProject", Map.of("project_id", projectId));
    }

    @Tool(description = "Get all tags assigned to a task.")
    public String getTaskTags(
            @ToolParam(description = "The numeric ID of the task") int taskId) {
        return client.executePretty("getTaskTags", Map.of("task_id", taskId));
    }

    @Tool(description = "Set tags on a task, replacing any existing tags. Pass an empty list to remove all tags.")
    public String setTaskTags(
            @ToolParam(description = "The numeric ID of the project the task belongs to") int projectId,
            @ToolParam(description = "The numeric ID of the task") int taskId,
            @ToolParam(description = "List of tag names to assign to the task") List<String> tags) {
        return client.executePretty("setTaskTags", Map.of(
                "project_id", projectId,
                "task_id", taskId,
                "tags", tags
        ));
    }
}
