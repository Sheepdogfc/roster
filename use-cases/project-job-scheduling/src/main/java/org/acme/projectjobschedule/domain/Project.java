package org.acme.projectjobschedule.domain;

import java.util.List;

import org.acme.projectjobschedule.domain.resource.LocalResource;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Project {

    private String id;
    private int releaseDate;
    private int criticalPathDuration;

    private List<LocalResource> localResources;
    private List<Job> jobs;

    public Project() {
    }

    public Project(String id) {
        this.id = id;
    }

    public Project(String id, int releaseDate, int criticalPathDuration) {
        this(id);
        this.releaseDate = releaseDate;
        this.criticalPathDuration = criticalPathDuration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(int releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getCriticalPathDuration() {
        return criticalPathDuration;
    }

    public void setCriticalPathDuration(int criticalPathDuration) {
        this.criticalPathDuration = criticalPathDuration;
    }

    public List<LocalResource> getLocalResources() {
        return localResources;
    }

    public void setLocalResources(List<LocalResource> localResources) {
        this.localResources = localResources;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonIgnore
    public int getCriticalPathEndDate() {
        return releaseDate + criticalPathDuration;
    }

}
