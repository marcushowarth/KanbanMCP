package eu.howarth.mcp.kanban.client;

import eu.howarth.mcp.kanban.config.KanboardProperties;
import io.quarkiverse.mcp.server.ToolCallException;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Picks the Kanboard account for the current call. The bearer path has no
 * identity in the app (Caddy checks the token) and runs as the default account.
 * An OAuth caller runs as the account mapped to their username in
 * {@code kanboard.users}, and is refused if there isn't one (a tool error the
 * caller can read, rather than the default account).
 *
 * The identity is read from the request's already-authenticated user, not from
 * {@code SecurityIdentity} injection: asking for the identity on the bearer path
 * makes OIDC try to authenticate Caddy's static token, and the call fails.
 */
@ApplicationScoped
public class KanboardCredentials {

    public record Credentials(String username, String apiToken) {
        @Override
        public String toString() {
            return "Credentials[username=" + username + "]";
        }
    }

    private final KanboardProperties properties;
    private final CurrentVertxRequest currentRequest;

    public KanboardCredentials(KanboardProperties properties, CurrentVertxRequest currentRequest) {
        this.properties = properties;
        this.currentRequest = currentRequest;
    }

    public Credentials current() {
        var request = currentRequest.getCurrent();
        if (request != null && request.user() instanceof QuarkusHttpUser user) {
            return resolve(user.getSecurityIdentity());
        }
        return resolve(null);
    }

    Credentials resolve(SecurityIdentity identity) {
        if (identity == null || identity.isAnonymous()) {
            return new Credentials(properties.username(), properties.apiToken());
        }
        String name = identity.getPrincipal().getName();
        KanboardProperties.User user = properties.users().get(name);
        if (user == null) {
            throw new ToolCallException("No Kanboard account is mapped for OAuth user '" + name + "'");
        }
        return new Credentials(user.username(), user.apiToken());
    }
}
