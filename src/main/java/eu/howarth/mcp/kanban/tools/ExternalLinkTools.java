package eu.howarth.mcp.kanban.tools;

import eu.howarth.mcp.kanban.client.KanboardClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ExternalLinkTools {

    private final KanboardClient client;

    public ExternalLinkTools(KanboardClient client) {
        this.client = client;
    }

    @Tool(description = "Get all external links attached to a task. Returns URLs, titles, and dependency types.")
    public String getAllExternalTaskLinks(
            @ToolParam(description = "The numeric ID of the task") int taskId) {
        return client.executePretty("getAllExternalTaskLinks", Map.of("task_id", taskId));
    }

    @Tool(description = "Add an external link to a task. Use this to attach a URL such as a GitHub commit, PR, or any web resource. Dependency is typically 'related'.")
    public String createExternalTaskLink(
            @ToolParam(description = "The numeric ID of the task") int taskId,
            @ToolParam(description = "The full URL to link to") String url,
            @ToolParam(description = "Link title shown in the UI") String title,
            @ToolParam(description = "Dependency type: 'related', 'blocks', or 'blocked_by' (default: 'related')", required = false) String dependency) {
        return client.executePretty("createExternalTaskLink", Map.of(
                "task_id", taskId,
                "url", url,
                "title", title,
                "dependency", dependency != null ? dependency : "related"
        ));
    }

    @Tool(description = "Remove an external link from a task by its link ID.")
    public String removeExternalTaskLink(
            @ToolParam(description = "The numeric ID of the task") int taskId,
            @ToolParam(description = "The numeric ID of the external link to remove") int linkId) {
        return client.executePretty("removeExternalTaskLink", Map.of(
                "task_id", taskId,
                "link_id", linkId
        ));
    }
}
