package eu.howarth.mcp.kanban.tools;

import eu.howarth.mcp.kanban.client.KanboardClient;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import io.quarkiverse.mcp.server.McpServer;
import static io.quarkiverse.mcp.server.McpServer.DEFAULT;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
@McpServer(DEFAULT)
@McpServer("oauth")
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
            @ToolArg(description = "The numeric ID of the project") int projectId) {
        return client.executePretty("getProjectById", Map.of("project_id", projectId));
    }
}
