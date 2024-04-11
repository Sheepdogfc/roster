package org.acme.taskassigning.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.bendable.BendableScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

@PlanningSolution
public class TaskAssigningSolution {

    @ProblemFactCollectionProperty
    private List<Customer> customers;

    @ValueRangeProvider
    @PlanningEntityCollectionProperty
    private List<Task> tasks;

    @PlanningEntityCollectionProperty
    private List<Employee> employees;

    @PlanningScore(bendableHardLevelsSize = 1, bendableSoftLevelsSize = 5)
    private BendableScore score;

    private SolverStatus solverStatus;

    /** Relates to {@link Task#getStartTime()}. */
    private int frozenCutoff; // In minutes

    public TaskAssigningSolution() {
    }

    public TaskAssigningSolution(BendableScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public BendableScore getScore() {
        return score;
    }

    public void setScore(BendableScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }

    public int getFrozenCutoff() {
        return frozenCutoff;
    }

    public void setFrozenCutoff(int frozenCutoff) {
        this.frozenCutoff = frozenCutoff;
    }

}
