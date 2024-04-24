package org.acme.schooltimetabling.rest;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.schooltimetabling.domain.Timetable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import io.restassured.http.ContentType;

@SpringBootTest(properties = {
        "timefold.solver.environment-mode=FAST_ASSERT",
        "quarkus.timefold.solver.termination.spent-limit=30s" },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("slowly")
class TimetableFullAssertTest {

    @LocalServerPort
    private int port;

    @Test
    void solve() {
        Timetable testTimetable = given()
                .port(port)
                .when().get("/demo-data/SMALL")
                .then()
                .statusCode(200)
                .extract()
                .as(Timetable.class);

        String jobId = given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(testTimetable)
                .expect().contentType(ContentType.TEXT)
                .when().post("/timetables")
                .then()
                .statusCode(200)
                .extract()
                .asString();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> SolverStatus.NOT_SOLVING == given()
                        .port(port)
                        .when().get("/timetables/" + jobId + "/status")
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Timetable.class)
                        .getSolverStatus());

        Timetable solution = given()
                .port(port)
                .when().get("/timetables/" + jobId)
                .then()
                .statusCode(200)
                .extract()
                .as(Timetable.class);
        assertTrue(solution.getScore().isFeasible());
    }

}