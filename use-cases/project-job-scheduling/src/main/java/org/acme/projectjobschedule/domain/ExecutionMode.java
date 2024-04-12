package org.acme.projectjobschedule.domain;

import java.util.List;

public class ExecutionMode {

    private String id;
    private Job job;
    private int duration; // In days

    public ExecutionMode() {
    }

    public ExecutionMode(String id) {
        this.id = id;
    }

    public ExecutionMode(String id, Job job) {
        this(id);
        this.job = job;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private List<ResourceRequirement> resourceRequirementList;

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public List<ResourceRequirement> getResourceRequirementList() {
        return resourceRequirementList;
    }

    public void setResourceRequirementList(List<ResourceRequirement> resourceRequirementList) {
        this.resourceRequirementList = resourceRequirementList;
    }

}
