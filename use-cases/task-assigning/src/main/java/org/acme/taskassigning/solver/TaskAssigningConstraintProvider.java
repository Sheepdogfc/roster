package org.acme.taskassigning.solver;

import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.uni.UniConstraintStream;

import org.acme.taskassigning.domain.Employee;
import org.acme.taskassigning.domain.Priority;
import org.acme.taskassigning.domain.Task;

public class TaskAssigningConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                noMissingSkills(constraintFactory),
                minimizeUnassignedTasks(constraintFactory),
                minimizeMakespan(constraintFactory),
                criticalPriorityTaskEndTime(constraintFactory),
                majorPriorityTaskEndTime(constraintFactory),
                minorPriorityTaskEndTime(constraintFactory)
        };
    }

    protected Constraint noMissingSkills(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Task.class)
                .filter(task -> task.getMissingSkillCount() > 0)
                .penalize(HardMediumSoftScore.ONE_HARD,
                        Task::getMissingSkillCount)
                .asConstraint("No missing skills");
    }

    protected Constraint minimizeUnassignedTasks(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingUnassigned(Task.class)
                .filter(task -> task.getEmployee() == null)
                .penalize(HardMediumSoftScore.ONE_MEDIUM)
                .asConstraint("Minimize unassigned tasks");
    }

    private UniConstraintStream<Task> getTaskWithPriority(ConstraintFactory constraintFactory, Priority priority) {
        return constraintFactory.forEach(Task.class)
                .filter(task -> task.getEmployee() != null)
                .filter(task -> task.getPriority() == priority);
    }

    protected Constraint minimizeMakespan(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .penalize(HardMediumSoftScore.ONE_SOFT, employee -> employee.getEndTime() * employee.getEndTime())
                .asConstraint("Minimize makespan, latest ending employee first");
    }

    protected Constraint criticalPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getTaskWithPriority(constraintFactory, Priority.CRITICAL)
                .penalize(HardMediumSoftScore.ONE_SOFT, Task::getEndTime)
                .asConstraint("Critical priority task end time");
    }

    protected Constraint majorPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getTaskWithPriority(constraintFactory, Priority.MAJOR)
                .penalize(HardMediumSoftScore.ONE_SOFT, Task::getEndTime)
                .asConstraint("Major priority task end time");
    }

    protected Constraint minorPriorityTaskEndTime(ConstraintFactory constraintFactory) {
        return getTaskWithPriority(constraintFactory, Priority.MINOR)
                .penalize(HardMediumSoftScore.ONE_SOFT, Task::getEndTime)
                .asConstraint("Minor priority task end time");
    }
}
