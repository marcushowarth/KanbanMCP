package eu.howarth.mcp.kanban.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "kanboard")
public interface KanboardProperties {
    String url();

    String apiToken();

    String username();

    int userId();
}
