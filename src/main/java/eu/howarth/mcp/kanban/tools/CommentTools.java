package eu.howarth.mcp.kanban.tools;

import com.fasterxml.jackson.databind.JsonNode;
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
public class CommentTools {

    private final KanboardClient client;

    public CommentTools(KanboardClient client) {
        this.client = client;
    }

    @Tool(description = "Get all comments on a specific task. Each comment includes its own id (needed for updateComment and removeComment), the author (user_id plus username and name), the content, and creation/modification timestamps. IMPORTANT: this server can only edit or remove comments authored by the Kanboard account it is calling as; comments written by anyone else, e.g. a human in the web UI, cannot be changed. Check each comment's user_id before attempting to update or remove it.")
    public String getAllComments(
            @ToolArg(description = "The numeric ID of the task") int taskId) {
        return client.executePretty("getAllComments", Map.of("task_id", taskId));
    }

    @Tool(description = "Add a new comment to a task. Use this to leave notes, updates, or discussion on a task. The comment is authored by the Kanboard account this server is calling as, so it is the only comment type this server can later edit or remove.")
    public String createComment(
            @ToolArg(description = "The numeric ID of the task") int taskId,
            @ToolArg(description = "The comment text content") String content) {
        return client.executePretty("createComment", Map.of(
                "task_id", taskId,
                "user_id", client.currentUserId(),
                "content", content
        ));
    }

    @Tool(description = "Update the content of an existing comment. This REPLACES the entire comment content, it does not append — always call getAllComments first to read the current text, then send the full intended content. IMPORTANT: this only works on comments authored by the account this server is calling as; Kanboard forbids editing a comment authored by anyone else (e.g. one written by a human in the web UI), so check the comment's user_id first and do not attempt it otherwise.")
    public String updateComment(
            @ToolArg(description = "The numeric ID of the comment to update (obtain it from getAllComments)") int id,
            @ToolArg(description = "The full new comment text. This overwrites the existing content entirely.") String content) {
        JsonNode result = client.execute("updateComment", Map.of(
                "id", id,
                "content", content
        ));
        if (result != null && result.asBoolean(false)) {
            return "Comment " + id + " updated.";
        }
        return "Update failed: Kanboard returned false. Comment " + id
                + " was almost certainly created by a different user — this server can only edit comments authored by the account it calls as. "
                + "Verify the comment's user_id via getAllComments.";
    }

    @Tool(description = "Permanently remove a comment from a task. This cannot be undone. IMPORTANT: this only works on comments authored by the account this server is calling as; Kanboard forbids removing a comment authored by anyone else (e.g. one written by a human in the web UI), so check the comment's user_id first and do not attempt it otherwise.")
    public String removeComment(
            @ToolArg(description = "The numeric ID of the comment to remove (obtain it from getAllComments)") int id) {
        JsonNode result = client.execute("removeComment", Map.of("comment_id", id));
        if (result != null && result.asBoolean(false)) {
            return "Comment " + id + " removed.";
        }
        return "Removal failed: Kanboard returned false. The comment may not exist, or it was created by a different user — "
                + "this server can only remove comments authored by the account it calls as. Verify the comment's user_id via getAllComments.";
    }
}
