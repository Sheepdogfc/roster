package org.acme.employeescheduling.rest;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.Shift;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;
import io.restassured.http.ContentType;

@QuarkusTest
@TestProfile(EmployeeSchedulingFullAssertTest.FullAssertProfile.class)
class EmployeeSchedulingFullAssertTest {

    @Test
    void solveDemoDataUntilFeasible() {
        EmployeeSchedule testSchedule = given()
                .when().get("/demo-data/SMALL")
                .then()
                .statusCode(200)
                .extract()
                .as(EmployeeSchedule.class);

        String jobId = given()
                .contentType(ContentType.JSON)
                .body(testSchedule)
                .expect().contentType(ContentType.TEXT)
                .when().post("/schedules")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        await()
                .atMost(Duration.ofMinutes(5))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> SolverStatus.NOT_SOLVING.name().equals(
                        get("/schedules/" + jobId + "/status")
                                .jsonPath().get("solverStatus")));

        EmployeeSchedule solution = get("/schedules/" + jobId).then().extract().as(EmployeeSchedule.class);
        assertTrue(solution.getScore().isFeasible());
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