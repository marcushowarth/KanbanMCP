package eu.howarth.mcp.kanban.tools;

import eu.howarth.mcp.kanban.client.KanboardClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CategoryTools {

    private final KanboardClient client;

    public CategoryTools(KanboardClient client) {
        this.client = client;
    }

    @Tool(description = "Get all categories defined in a project. Returns each category's id and name. Categories are project-specific; use a category id with createTask or updateTask (categoryId) to classify a task.")
    public String getAllCategories(
            @ToolParam(description = "The numeric ID of the project") int projectId) {
        return client.executePretty("getAllCategories", Map.of("project_id", projectId));
    }
}
