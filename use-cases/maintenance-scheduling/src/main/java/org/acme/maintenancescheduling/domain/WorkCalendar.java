package org.acme.maintenancescheduling.domain;

import java.time.LocalDate;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class WorkCalendar {

    @PlanningId
    private String id;

    private LocalDate fromDate; // Inclusive
    private LocalDate toDate; // Exclusive

    // No-arg constructor required for Hibernate
    public WorkCalendar() {
    }

    public WorkCalendar(LocalDate fromDate, LocalDate toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    @Override
    public String toString() {
        return fromDate + " - " + toDate;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

}
