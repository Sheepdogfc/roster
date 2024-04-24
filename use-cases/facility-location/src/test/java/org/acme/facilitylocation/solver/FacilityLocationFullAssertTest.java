package org.acme.facilitylocation.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;

import org.acme.facilitylocation.bootstrap.DemoDataBuilder;
import org.acme.facilitylocation.domain.FacilityLocationProblem;
import org.acme.facilitylocation.domain.Location;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(FacilityLocationFullAssertTest.FullAssertProfile.class)
@Tag("slowly")
class FacilityLocationFullAssertTest {

    @Inject
    SolverManager<FacilityLocationProblem, Long> solverManager;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        FacilityLocationProblem problem = DemoDataBuilder.builder()
                .setCapacity(1200)
                .setDemand(900)
                .setAverageSetupCost(1000).setSetupCostStandardDeviation(200)
                .setFacilityCount(10)
                .setConsumerCount(150)
                .setSouthWestCorner(new Location(-10, -10))
                .setNorthEastCorner(new Location(10, 10))
                .build();

        FacilityLocationProblem solution = solverManager.solveBuilder()
                .withProblemId(0L)
                .withProblemFinder(id -> problem)
                .withFinalBestSolutionConsumer(SolverManagerTest::printSolution)
                .run()
                .getFinalBestSolution();
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