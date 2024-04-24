package org.acme.orderpicking.rest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverManager;

import org.acme.orderpicking.domain.OrderPickingSolution;
import org.acme.orderpicking.persistence.OrderPickingRepository;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(OrderPickingFullAssertTest.FullAssertProfile.class)
class OrderPickingFullAssertTest {

    @Inject
    OrderPickingRepository repository;
    @Inject
    SolverManager<OrderPickingSolution, String> solverManager;

    @Test
    void solve() throws ExecutionException, InterruptedException {
        OrderPickingSolution problem = repository.find();

        OrderPickingSolution solution = solverManager.solveBuilder()
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
                    "quarkus.timefold.solver.environment-mode", "FAST_ASSERT",
                    "quarkus.timefold.solver.termination.best-score-limit", "",
                    "quarkus.timefold.solver.termination.spent-limit", "30s");
        }
    }

}