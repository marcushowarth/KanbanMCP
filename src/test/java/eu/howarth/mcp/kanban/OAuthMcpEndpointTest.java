package eu.howarth.mcp.kanban;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

/**
 * The OAuth-secured MCP entry point (Claude Connectors — Desktop/web/mobile),
 * separate from the existing bearer-token path used by Claude Code
 * ({@code /kanban/mcp}, gated at Caddy, unaffected by this).
 *
 * An unauthenticated request must get the RFC 9728 handshake: a 401 with a
 * {@code WWW-Authenticate} header pointing at the protected-resource-metadata
 * document, so Claude knows where to find the authorization server.
 */
@QuarkusTest
class OAuthMcpEndpointTest {

    @BeforeEach
    void resetBasePath() {
        // @QuarkusTest defaults RestAssured.basePath to quarkus.http.root-path (/kanban);
        // reset it so the literal path below isn't doubled up, matching KanbanMcpHealthIT.
        RestAssured.basePath = "";
    }

    @Test
    void unauthenticatedRequestGetsResourceMetadataChallenge() {
        given()
                .when().post("/kanban/oauth/mcp")
                .then()
                .statusCode(401)
                .header("WWW-Authenticate", containsString("resource_metadata"));
    }
}
