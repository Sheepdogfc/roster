package org.acme.taskassigning.rest;

import java.util.Random;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.taskassigning.domain.TaskAssigningSolution;

@ApplicationScoped
public class DemoDataGenerator {

    private final Random random = new Random(0);

    public TaskAssigningSolution generateDemoData() {
        TaskAssigningSolution schedule = new TaskAssigningSolution();

        return schedule;
    }
}
