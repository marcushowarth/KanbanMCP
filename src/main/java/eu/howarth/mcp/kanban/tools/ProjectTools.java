package eu.howarth.mcp.kanban.tools;

import eu.howarth.mcp.kanban.client.KanboardClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProjectTools {

    private final KanboardClient client;

    public ProjectTools(KanboardClient client) {
        this.client = client;
    }

    @Tool(description = "Get all projects accessible to the current user. Returns a list of projects with their IDs, names, and status.")
    public String getAllProjects() {
        return client.executePretty("getAllProjects");
    }

    @Tool(description = "Get detailed information about a specific project by its ID, including name, description, board URL, and status.")
    public String getProjectById(
            @ToolParam(description = "The numeric ID of the project") int projectId) {
        return client.executePretty("getProjectById", Map.of("project_id", projectId));
    }
}
