package org.acme.projectjobschedule.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;

import org.acme.projectjobschedule.domain.resource.Resource;

@PlanningSolution
public class Schedule {

    @ProblemFactCollectionProperty
    private List<Project> projects;
    @ProblemFactCollectionProperty
    private List<Job> jobs;
    @ProblemFactCollectionProperty
    private List<ExecutionMode> executionModes;
    @ProblemFactCollectionProperty
    private List<Resource> resources;
    @ProblemFactCollectionProperty
    private List<ResourceRequirement> resourceRequirements;

    @PlanningEntityCollectionProperty
    private List<Allocation> allocations;

    @PlanningScore
    private HardMediumSoftScore score;

    public Schedule() {
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public List<ExecutionMode> getExecutionModes() {
        return executionModes;
    }

    public void setExecutionModes(List<ExecutionMode> executionModes) {
        this.executionModes = executionModes;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public List<ResourceRequirement> getResourceRequirements() {
        return resourceRequirements;
    }

    public void setResourceRequirements(List<ResourceRequirement> resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
    }

    public List<Allocation> getAllocations() {
        return allocations;
    }

    public void setAllocations(List<Allocation> allocations) {
        this.allocations = allocations;
    }

    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

}
