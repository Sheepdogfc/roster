package org.acme.taskassigning.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.bendable.BendableScore;

@PlanningSolution
public class TaskAssigningSolution {

    @ProblemFactCollectionProperty
    private List<Customer> customerList;

    @ValueRangeProvider
    @PlanningEntityCollectionProperty
    private List<Task> taskList;

    @PlanningEntityCollectionProperty
    private List<Employee> employeeList;

    @PlanningScore(bendableHardLevelsSize = 1, bendableSoftLevelsSize = 5)
    private BendableScore score;

    /** Relates to {@link Task#getStartTime()}. */
    private int frozenCutoff; // In minutes

    public TaskAssigningSolution() {
    }

    public List<Customer> getCustomerList() {
        return customerList;
    }

    public void setCustomerList(List<Customer> customerList) {
        this.customerList = customerList;
    }

    public List<Employee> getEmployeeList() {
        return employeeList;
    }

    public void setEmployeeList(List<Employee> employeeList) {
        this.employeeList = employeeList;
    }

    public List<Task> getTaskList() {
        return taskList;
    }

    public void setTaskList(List<Task> taskList) {
        this.taskList = taskList;
    }

    public BendableScore getScore() {
        return score;
    }

    public void setScore(BendableScore score) {
        this.score = score;
    }

    public int getFrozenCutoff() {
        return frozenCutoff;
    }

    public void setFrozenCutoff(int frozenCutoff) {
        this.frozenCutoff = frozenCutoff;
    }

}
