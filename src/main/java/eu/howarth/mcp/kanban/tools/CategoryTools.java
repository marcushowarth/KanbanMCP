package eu.howarth.mcp.kanban.tools;

import eu.howarth.mcp.kanban.client.KanboardClient;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class CategoryTools {

    private final KanboardClient client;

    public CategoryTools(KanboardClient client) {
        this.client = client;
    }

    @Tool(description = "Get all categories defined in a project. Returns each category's id and name. Categories are project-specific; use a category id with createTask or updateTask (categoryId) to classify a task.")
    public String getAllCategories(
            @ToolArg(description = "The numeric ID of the project") int projectId) {
        return client.executePretty("getAllCategories", Map.of("project_id", projectId));
    }
}
