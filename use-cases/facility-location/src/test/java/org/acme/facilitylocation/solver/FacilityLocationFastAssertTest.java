package org.acme.facilitylocation.solver;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(FacilityLocationFastAssertTest.FastAssertProfile.class)
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class FacilityLocationFastAssertTest {

    @Test
    void solve() throws ExecutionException, InterruptedException {
        post("/flp/solve")
                .then()
                .statusCode(204)
                .extract();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> !get("/flp/status").jsonPath().getBoolean("isSolving"));

        String score = get("/flp/status").jsonPath().get("solution.score");
        assertThat(score).isNotNull();
    }

    public static class FastAssertProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of(
                    "quarkus.timefold.solver.environment-mode", "FAST_ASSERT",
                    "quarkus.timefold.solver.termination.best-score-limit", "",
                    "quarkus.timefold.solver.termination.spent-limit", "30s");
        }
    }

}