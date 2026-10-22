package eu.howarth.mcp.kanban.client;

import eu.howarth.mcp.kanban.config.KanboardProperties;
import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Which Kanboard account a call runs as. The bearer path (Claude Code, gated at
 * Caddy) has no identity in the app and uses the default account. An OAuth caller
 * must have their own mapped account: an unmapped one is refused rather than
 * quietly given the default account, which can see boards they shouldn't.
 */
class KanboardCredentialsTest {

    record User(String username, String apiToken) implements KanboardProperties.User {
    }

    static KanboardProperties properties(Map<String, KanboardProperties.User> users) {
        return new KanboardProperties() {
            public String url() { return "https://kanboard.example/jsonrpc.php"; }
            public String apiToken() { return "default-token"; }
            public String username() { return "claude"; }
            public Map<String, User> users() {
                @SuppressWarnings({"unchecked", "rawtypes"})
                Map<String, User> typed = (Map) users;
                return typed;
            }
        };
    }

    private final KanboardCredentials credentials = new KanboardCredentials(
            properties(Map.of("iris", new User("iris", "iris-token"))), null);

    private static QuarkusSecurityIdentity oauthUser(String name) {
        return QuarkusSecurityIdentity.builder().setPrincipal(new QuarkusPrincipal(name)).build();
    }

    @Test
    void anonymousCallerUsesTheDefaultAccount() {
        var result = credentials.resolve(QuarkusSecurityIdentity.builder().setAnonymous(true).build());
        assertEquals(new KanboardCredentials.Credentials("claude", "default-token"), result);
    }

    @Test
    void noIdentityUsesTheDefaultAccount() {
        assertEquals("claude", credentials.resolve(null).username());
    }

    @Test
    void mappedOAuthUserUsesTheirOwnAccount() {
        assertEquals(new KanboardCredentials.Credentials("iris", "iris-token"),
                credentials.resolve(oauthUser("iris")));
    }

    @Test
    void unmappedOAuthUserIsRefused() {
        var e = assertThrows(ToolCallException.class, () -> credentials.resolve(oauthUser("someone")));
        assertTrue(e.getMessage().contains("someone"));
        assertFalse(e.getMessage().contains("default-token"));
    }
}
