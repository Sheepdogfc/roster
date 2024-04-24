package org.acme.schooltimetabling.solver;

import static org.acme.schooltimetabling.TimetableApp.generateDemoData;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import ai.timefold.solver.core.api.solver.Solver;
import ai.timefold.solver.core.api.solver.SolverFactory;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.config.solver.SolverConfig;

import org.acme.schooltimetabling.TimetableApp;
import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Timetable;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("slowly")
class TimetableFullAssertTest {

    @Test
    void solve() {
        SolverFactory<Timetable> solverFactory = SolverFactory.create(new SolverConfig()
                .withSolutionClass(Timetable.class)
                .withEntityClasses(Lesson.class)
                .withConstraintProviderClass(TimetableConstraintProvider.class)
                .withEnvironmentMode(EnvironmentMode.FAST_ASSERT)
                .withTerminationSpentLimit(Duration.ofSeconds(30))
        );

        // Load the problem
        Timetable problem = generateDemoData(TimetableApp.DemoData.SMALL);

        // Solve the problem
        Solver<Timetable> solver = solverFactory.buildSolver();
        Timetable solution = solver.solve(problem);
        assertThat(solution.getScore().isFeasible()).isTrue();
    }

}