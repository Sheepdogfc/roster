package org.acme.projectjobschedule.rest;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.projectjobschedule.domain.Project;
import org.acme.projectjobschedule.domain.Schedule;
import org.acme.projectjobschedule.domain.resource.GlobalResource;
import org.acme.projectjobschedule.domain.resource.LocalResource;
import org.acme.projectjobschedule.domain.resource.Resource;

@ApplicationScoped
public class DemoDataGenerator {

    private final static Project FIRST_PROJECT = new Project("0", 0, 10);
    private final static Project SECOND_PROJECT = new Project("1", 4, 19);

    public Schedule generateDemoData() {
        Schedule schedule = new Schedule();

        List<Project> projects = List.of(FIRST_PROJECT, SECOND_PROJECT);

        List<Resource> resources = List.of(
                new GlobalResource("0", 16)
        );

        // Update schedule
        schedule.setProjects(projects);
        return schedule;
    }
}
