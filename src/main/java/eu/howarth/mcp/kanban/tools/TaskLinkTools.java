package eu.howarth.mcp.kanban.tools;

import eu.howarth.mcp.kanban.client.KanboardClient;
import io.quarkiverse.mcp.server.Tool;
import io.quarkiverse.mcp.server.ToolArg;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class TaskLinkTools {

    private final KanboardClient client;

    public TaskLinkTools(KanboardClient client) {
        this.client = client;
    }

    @Tool(description = "Get all internal links between this task and other tasks. Returns each link's id (needed for removeTaskLink), the linked (opposite) task, and the relationship label. These are task-to-task relationships, not external URLs.")
    public String getAllTaskLinks(
            @ToolArg(description = "The numeric ID of the task") int taskId) {
        return client.executePretty("getAllTaskLinks", Map.of("task_id", taskId));
    }

    @Tool(description = "Create an internal link relating one task to another. The link_id sets the relationship type. Kanboard's default link types are: 1=relates to, 2=blocks, 3=is blocked by, 4=duplicates, 5=is duplicated by, 6=is a child of, 7=is a parent of, 8=targets milestone, 9=is a milestone of, 10=fixes, 11=is fixed by.")
    public String createTaskLink(
            @ToolArg(description = "The numeric ID of the task the link is added to") int taskId,
            @ToolArg(description = "The numeric ID of the other (opposite) task to link to") int oppositeTaskId,
            @ToolArg(description = "The relationship type id (e.g. 1=relates to, 2=blocks, 3=is blocked by)") int linkId) {
        return client.executePretty("createTaskLink", Map.of(
                "task_id", taskId,
                "opposite_task_id", oppositeTaskId,
                "link_id", linkId
        ));
    }

    @Tool(description = "Remove an internal task link by its link id (the 'id' field returned by getAllTaskLinks, not a task id).")
    public String removeTaskLink(
            @ToolArg(description = "The numeric ID of the task link to remove (from getAllTaskLinks)") int taskLinkId) {
        return client.executePretty("removeTaskLink", Map.of("task_link_id", taskLinkId));
    }
}
