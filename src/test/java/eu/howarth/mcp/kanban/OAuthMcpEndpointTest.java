package eu.howarth.mcp.kanban;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

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

    @Inject
    @ConfigProperty(name = "quarkus.oidc.auth-server-url")
    String authServerUrl;

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

    @Test
    void validTokenReachesTheEndpoint() {
        String accessToken = given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "kanban-mcp-test-client")
                .formParam("username", "test-user")
                .formParam("password", "test-password")
                .when().post(authServerUrl + "/protocol/openid-connect/token")
                .then().statusCode(200)
                .extract().path("access_token");

        given()
                .auth().oauth2(accessToken)
                .when().post("/kanban/oauth/mcp")
                .then()
                // Not asserting 200 here — an MCP streamable-HTTP POST needs a real
                // JSON-RPC body to get a 200. The point of this test is that a valid
                // token clears authentication: proven by NOT getting 401 again.
                .statusCode(org.hamcrest.Matchers.not(401));
    }
}
