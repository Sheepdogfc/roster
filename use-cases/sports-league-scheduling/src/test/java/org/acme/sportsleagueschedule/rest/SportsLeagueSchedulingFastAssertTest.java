package org.acme.sportsleagueschedule.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Map;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.sportsleagueschedule.domain.LeagueSchedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;

@QuarkusTest
@TestProfile(SportsLeagueSchedulingFastAssertTest.FullAssertProfile.class)
@EnabledIfSystemProperty(named = "slowly", matches = "true")
class SportsLeagueSchedulingFastAssertTest {

    @Test
    void solve() {
        LeagueSchedule schedule = given()
                .when().get("/demo-data")
                .then()
                .statusCode(200)
                .extract()
                .as(LeagueSchedule.class);

        String jobId = given()
                .contentType(ContentType.JSON)
                .body(schedule)
                .expect().contentType(ContentType.TEXT)
                .when().post("/schedules")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> SolverStatus.NOT_SOLVING.name().equals(
                        get("/schedules/" + jobId + "/status")
                                .jsonPath().get("solverStatus")));

        LeagueSchedule solution = get("/schedules/" + jobId).then().extract().as(LeagueSchedule.class);
        assertThat(solution.getScore().isFeasible()).isTrue();
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