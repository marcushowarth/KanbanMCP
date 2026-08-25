package eu.howarth.mcp.kanban;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Inject
    @ConfigProperty(name = "quarkus.oidc.resource-metadata.resource")
    String configuredResourceUrl;

    @Test
    void protectedResourceMetadataResourceIsConfiguredToMatchTheConnectorUrlExactly() {
        // Anthropic requires the RFC 9728 "resource" field to match the Connector URL
        // exactly, including path — Quarkus defaults this to the bare origin otherwise,
        // which is wrong for a server that isn't mounted at "/". Caught live in prod
        // 2026-08-25 (a real Claude Connector would have rejected the mismatch).
        //
        // This asserts the CONFIG VALUE rather than hitting the live well-known route:
        // Quarkus only registers that route when auth-server-url is statically known at
        // build time, which Dev Services' runtime-injected value doesn't satisfy — the
        // route genuinely 404s in dev/test regardless of whether this config is correct.
        // Verified against the real deployed instance separately (curl, post-deploy).
        assertEquals("https://mcp.howarth.eu/kanban/oauth/mcp", configuredResourceUrl);
    }

    @Test
    void bearerPathIsNotInterceptedByOidc() {
        // Regression test for a real production incident (2026-08-25): the OIDC
        // config originally lived on the DEFAULT (unnamed) tenant with tenant-paths
        // restricting only /kanban/oauth/mcp — but the default tenant's filter still
        // ran for every request carrying an Authorization: Bearer header app-wide,
        // regardless of tenant-paths. That took down the pre-existing bearer-token
        // path used by Claude Code in production: a valid static token got a 401
        // OIDC challenge instead of reaching the app (Caddy is what actually gates
        // this path in prod — the app itself must impose no OIDC auth here at all).
        // Fixed by moving OIDC config onto a named tenant ("oauth"), which only
        // activates for requests actually matching its own tenant-paths.
        given()
                .header("Authorization", "Bearer some-static-non-jwt-token")
                .when().post("/kanban/mcp")
                .then()
                .header("WWW-Authenticate", org.hamcrest.Matchers.nullValue());
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
