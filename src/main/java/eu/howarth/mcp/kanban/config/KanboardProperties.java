package eu.howarth.mcp.kanban.config;

import io.smallrye.config.ConfigMapping;

import java.util.Map;

@ConfigMapping(prefix = "kanboard")
public interface KanboardProperties {
    String url();

    String apiToken();

    String username();

    /** Kanboard accounts for OAuth callers, keyed by their OAuth username. */
    Map<String, User> users();

    interface User {
        String username();

        String apiToken();
    }
}
