package org.acme.projectjobschedule.domain;

import java.util.List;

public class Job {

    private String id;
    private Project project;
    private JobType jobType;
    private List<ExecutionMode> executionModes;

    private List<Job> successorJobs;

    public Job() {
    }

    public Job(String id) {
        this.id = id;
    }

    public Job(String id, Project project) {
        this(id);
        this.project = project;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public List<ExecutionMode> getExecutionModes() {
        return executionModes;
    }

    public void setExecutionModes(List<ExecutionMode> executionModes) {
        this.executionModes = executionModes;
    }

    public List<Job> getSuccessorJobs() {
        return successorJobs;
    }

    public void setSuccessorJobs(List<Job> successorJobs) {
        this.successorJobs = successorJobs;
    }

}
