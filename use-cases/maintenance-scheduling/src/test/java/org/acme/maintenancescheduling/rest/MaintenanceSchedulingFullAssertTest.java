package org.acme.maintenancescheduling.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;

@QuarkusTest
@TestProfile(MaintenanceSchedulingFullAssertTest.FullAssertProfile.class)
@Tag("slowly")
class MaintenanceSchedulingFullAssertTest {

    @Test
    void solve() {
        MaintenanceSchedule maintenanceSchedule = given()
                .when().get("/demo-data/SMALL")
                .then()
                .statusCode(200)
                .extract()
                .as(MaintenanceSchedule.class);

        String jobId = given()
                .contentType(ContentType.JSON)
                .body(maintenanceSchedule)
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

        MaintenanceSchedule solution = get("/schedules/" + jobId).then().extract().as(MaintenanceSchedule.class);
        assertTrue(solution.getScore().isFeasible());
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