package org.acme.orderpicking.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(OrderPickingFullAssertTest.FullAssertProfile.class)
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class OrderPickingFullAssertTest {

    @Test
    void solve() {
        post("/orderPicking/solve")
                .then()
                .statusCode(204)
                .extract();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> !SolverStatus.NOT_SOLVING.name().equals(get("/orderPicking").jsonPath().get("solverStatus")));

        String score = get("/orderPicking").jsonPath().getString("solution.score");
        assertThat(score).isNotNull();
    }

    public static class FullAssertProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.timefold.solver.environment-mode", "FULL_ASSERT",
                    "quarkus.timefold.solver.termination.best-score-limit", "",
                    "quarkus.timefold.solver.termination.spent-limit", "30s");
        }
    }

}