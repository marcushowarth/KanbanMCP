package eu.howarth.mcp.kanban.config;

import eu.howarth.mcp.kanban.tools.BoardTools;
import eu.howarth.mcp.kanban.tools.CommentTools;
import eu.howarth.mcp.kanban.tools.ProjectTools;
import eu.howarth.mcp.kanban.tools.TaskTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider kanboardTools(
            ProjectTools projectTools,
            BoardTools boardTools,
            TaskTools taskTools,
            CommentTools commentTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(projectTools, boardTools, taskTools, commentTools)
                .build();
    }
}
