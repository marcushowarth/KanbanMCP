package eu.howarth.mcp.kanban.config;

import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.EnvConfigSource;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Per-user Kanboard accounts arrive as plain environment variables (a docker
 * {@code --env-file} on the host), keyed by the OAuth username. SmallRye has to
 * find a map key that exists only in env var names, so pin that it does.
 */
class KanboardPropertiesTest {

    private static KanboardProperties load(Map<String, String> env) {
        SmallRyeConfig config = new SmallRyeConfigBuilder()
                .withMapping(KanboardProperties.class)
                .withSources(new EnvConfigSource(env, 300))
                .build();
        return config.getConfigMapping(KanboardProperties.class);
    }

    private static final Map<String, String> DEFAULTS = Map.of(
            "KANBOARD_URL", "https://kanboard.example/jsonrpc.php",
            "KANBOARD_API_TOKEN", "default-token",
            "KANBOARD_USERNAME", "claude",
            "KANBOARD_USER_ID", "29");

    @Test
    void mappedUserIsReadFromEnvironmentVariables() {
        var env = new java.util.HashMap<>(DEFAULTS);
        env.put("KANBOARD_USERS_IRIS_USERNAME", "iris");
        env.put("KANBOARD_USERS_IRIS_API_TOKEN", "iris-token");

        KanboardProperties properties = load(env);

        assertEquals("iris", properties.users().get("iris").username());
        assertEquals("iris-token", properties.users().get("iris").apiToken());
    }

    @Test
    void noMappedUsersIsAllowed() {
        assertTrue(load(DEFAULTS).users().isEmpty());
    }
}
