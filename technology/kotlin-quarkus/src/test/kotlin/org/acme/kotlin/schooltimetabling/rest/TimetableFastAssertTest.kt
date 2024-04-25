package org.acme.kotlin.schooltimetabling.rest

import ai.timefold.solver.core.api.solver.SolverStatus
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.QuarkusTestProfile
import io.quarkus.test.junit.TestProfile
import io.restassured.RestAssured.get
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.acme.kotlin.schooltimetabling.domain.Timetable
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Duration

@QuarkusTest
@TestProfile(TimetableFastAssertTest.FullAssertProfile::class)
@Tag("slowly")
class TimetableFastAssertTest {

    @Test
    fun solve() {
        val testTimetable: Timetable = given()
            .`when`()["/demo-data/SMALL"]
            .then()
            .statusCode(200)
            .extract()
            .`as`(Timetable::class.java)

        val jobId: String = given()
            .contentType(ContentType.JSON)
            .body(testTimetable)
            .expect().contentType(ContentType.TEXT)
            .`when`().post("/timetables")
            .then()
            .statusCode(200)
            .extract()
            .asString()

        await()
            .atMost(Duration.ofMinutes(1))
            .pollInterval(Duration.ofMillis(500L))
            .until {
                SolverStatus.NOT_SOLVING.name ==
                        get("/timetables/$jobId/status")
                            .jsonPath().get("solverStatus")
            }
        val solution: Timetable =
            get("/timetables/$jobId").then().extract().`as`<Timetable>(
                Timetable::class.java
            )
        assertTrue(solution.score?.isFeasible!!)
    }

    class FullAssertProfile : QuarkusTestProfile {
        override fun getConfigOverrides(): Map<String, String> {
            return java.util.Map.of(
                "quarkus.timefold.solver.environment-mode", "FAST_ASSERT",
                "quarkus.timefold.solver.termination.best-score-limit", "",
                "quarkus.timefold.solver.termination.spent-limit", "30s"
            )
        }
    }
}
