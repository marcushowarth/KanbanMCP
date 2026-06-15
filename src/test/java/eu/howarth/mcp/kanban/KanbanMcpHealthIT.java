package eu.howarth.mcp.kanban;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

/**
 * Native smoke test: runs against the compiled native binary (not a Docker
 * container, so it sidesteps the OrbStack -p localhost-hang trap). Proves the
 * native image boots and that JSON-RPC DTO reflection registration didn't break
 * startup. Only runs under the 'native' profile (failsafe / -Dnative).
 */
@QuarkusIntegrationTest
class KanbanMcpHealthIT {

    @Test
    void healthIsUp() {
        RestAssured.basePath = "";
        given()
                .when().get("/kanban/q/health")
                .then().statusCode(200)
                .body("status", is("UP"));
    }
}
