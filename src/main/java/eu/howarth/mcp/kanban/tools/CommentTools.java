package eu.howarth.mcp.kanban.tools;

import eu.howarth.mcp.kanban.client.KanboardClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CommentTools {

    private final KanboardClient client;

    public CommentTools(KanboardClient client) {
        this.client = client;
    }

    @Tool(description = "Get all comments on a specific task. Returns a list of comments with author, content, and timestamps.")
    public String getAllComments(
            @ToolParam(description = "The numeric ID of the task") int taskId) {
        return client.executePretty("getAllComments", Map.of("task_id", taskId));
    }

    @Tool(description = "Add a new comment to a task. Use this to leave notes, updates, or discussion on a task.")
    public String createComment(
            @ToolParam(description = "The numeric ID of the task") int taskId,
            @ToolParam(description = "The user ID of the comment author") int userId,
            @ToolParam(description = "The comment text content") String content) {
        return client.executePretty("createComment", Map.of(
                "task_id", taskId,
                "user_id", userId,
                "content", content
        ));
    }

    @Tool(description = "Update the content of an existing comment. This REPLACES the entire comment content, it does not append. Always call getAllComments first to read the current text and the comment ID, then send the full intended content to avoid accidentally wiping it.")
    public String updateComment(
            @ToolParam(description = "The numeric ID of the comment to update (obtain it from getAllComments)") int id,
            @ToolParam(description = "The full new comment text. This overwrites the existing content entirely.") String content) {
        return client.executePretty("updateComment", Map.of(
                "id", id,
                "content", content
        ));
    }
}
