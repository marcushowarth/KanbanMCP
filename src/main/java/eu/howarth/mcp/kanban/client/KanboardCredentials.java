package eu.howarth.mcp.kanban.client;

import eu.howarth.mcp.kanban.config.KanboardProperties;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Picks the Kanboard account for the current call. The bearer path has no
 * identity in the app (Caddy checks the token) and runs as the default account.
 * An OAuth caller runs as the account mapped to their username in
 * {@code kanboard.users}, and is refused if there isn't one.
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
    private final CurrentIdentityAssociation identityAssociation;

    public KanboardCredentials(KanboardProperties properties, CurrentIdentityAssociation identityAssociation) {
        this.properties = properties;
        this.identityAssociation = identityAssociation;
    }

    public Credentials current() {
        return resolve(identityAssociation.getIdentity());
    }

    Credentials resolve(SecurityIdentity identity) {
        if (identity == null || identity.isAnonymous()) {
            return new Credentials(properties.username(), properties.apiToken());
        }
        String name = identity.getPrincipal().getName();
        KanboardProperties.User user = properties.users().get(name);
        if (user == null) {
            throw new KanboardException("No Kanboard account is mapped for OAuth user '" + name + "'");
        }
        return new Credentials(user.username(), user.apiToken());
    }
}
