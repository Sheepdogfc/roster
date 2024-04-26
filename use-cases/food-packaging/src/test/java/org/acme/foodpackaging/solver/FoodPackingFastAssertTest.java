package org.acme.foodpackaging.solver;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(FoodPackingFastAssertTest.FullAssertProfile.class)
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class FoodPackingFastAssertTest {

    @Test
    void solve() throws ExecutionException, InterruptedException {
        post("/schedule/solve")
                .then()
                .statusCode(204)
                .extract();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> !SolverStatus.NOT_SOLVING.name().equals(get("/schedule").jsonPath().get("solverStatus")));

        String score = get("/schedule").jsonPath().get("score");
        assertThat(score).isNotNull();
    }

    public static class FullAssertProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.timefold.solver.environment-mode", "FAST_ASSERT",
                    "quarkus.timefold.solver.termination.best-score-limit", "",
                    "quarkus.timefold.solver.termination.spent-limit", "30s");
        }
    }

}