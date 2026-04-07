package eu.howarth.mcp.kanban.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kanboard")
public record KanboardProperties(
        String url,
        String apiToken,
        String username
) {
}
