package org.acme.foodpackaging.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;

import org.acme.foodpackaging.domain.PackagingSchedule;
import org.acme.foodpackaging.persistence.PackagingScheduleRepository;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(FoodPackingFullAssertTest.FullAssertProfile.class)
class FoodPackingFullAssertTest {


    @Inject
    PackagingScheduleRepository repository;
    @Inject
    SolverManager<PackagingSchedule, String> solverManager;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        PackagingSchedule problem = repository.read();

        PackagingSchedule solution = solverManager.solveBuilder()
                .withProblemId("0")
                .withProblemFinder(id -> problem)
                .run()
                .getFinalBestSolution();
        assertThat(solution.getScore().isFeasible()).isTrue();
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