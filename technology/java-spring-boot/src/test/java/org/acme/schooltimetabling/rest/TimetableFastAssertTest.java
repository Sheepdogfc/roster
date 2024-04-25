package org.acme.schooltimetabling.rest;

import static org.awaitility.Awaitility.await;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.SolverStatus;

import org.acme.schooltimetabling.domain.Timetable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.JsonNode;

@SpringBootTest(properties = {
        "timefold.solver.environment-mode=FAST_ASSERT",
        "timefold.solver.termination.spent-limit=1s",
        "logging.level.ai.timefold.solver=ERROR" },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("slowly")
@DisabledInNativeImage
class TimetableFastAssertTest {

    @LocalServerPort
    private int port;

    @Test
    void solve() {
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        Timetable testTimetable = client.get()
                .uri("/demo-data/SMALL")
                .exchange()
                .expectBody(Timetable.class)
                .returnResult()
                .getResponseBody();

        String jobId = client.post()
                .uri("/timetables")
                .bodyValue(testTimetable)
                .exchange()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        await()
                .atMost(Duration.ofMinutes(1))
                .pollInterval(Duration.ofMillis(500L))
                .until(() -> SolverStatus.NOT_SOLVING.name().equals(
                        client.get()
                                .uri("/timetables/" + jobId + "/status")
                                .exchange()
                                .expectBody(JsonNode.class)
                                .returnResult()
                                .getResponseBody()
                                .get("solverStatus")
                                .asText()));

        client.get()
                .uri("/timetables/" + jobId)
                .exchange()
                .expectBody()
                .jsonPath("score").isNotEmpty();
    }

}