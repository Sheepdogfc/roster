package org.acme.projectjobschedule.solver;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.projectjobschedule.domain.Schedule;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ProjectJobSchedulingConstraintProviderTest {

    private final ConstraintVerifier<ProjectJobSchedulingConstraintProvider, Schedule> constraintVerifier;

    @Inject
    public ProjectJobSchedulingConstraintProviderTest(
            ConstraintVerifier<ProjectJobSchedulingConstraintProvider, Schedule> constraintVerifier) {
        this.constraintVerifier = constraintVerifier;
    }

    @Test
    void nonRenewableResourceCapacity() {

    }
}
