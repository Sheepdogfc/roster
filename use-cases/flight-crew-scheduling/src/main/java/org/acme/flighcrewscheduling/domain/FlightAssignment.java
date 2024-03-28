package org.acme.flighcrewscheduling.domain;

import java.util.Comparator;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class FlightAssignment implements Comparable<FlightAssignment> {

    // Needs to be kept consistent with equals on account of Employee's flightAssignmentSet, which is a SortedSet.
    private static final Comparator<FlightAssignment> COMPARATOR = Comparator.comparing(FlightAssignment::getFlight)
            .thenComparing(FlightAssignment::getIndexInFlight);

    @PlanningId
    private String id;
    private Flight flight;
    private int indexInFlight;
    private Skill requiredSkill;

    @PlanningVariable
    private Employee employee;

    public FlightAssignment() {
    }

    public FlightAssignment(String id, Flight flight) {
        this.id = id;
        this.flight = flight;
    }

    public FlightAssignment(String id, Flight flight, int indexInFlight, Skill requiredSkill) {
        this.id = id;
        this.flight = flight;
        this.indexInFlight = indexInFlight;
        this.requiredSkill = requiredSkill;
    }

    public long getFlightDurationInMinutes() {
        return flight.getDurationInMinutes();
    }

    public boolean hasRequiredSkills() {
        return getEmployee().hasSkill(requiredSkill);
    }

    public boolean isUnavailableEmployee() {
        return !getEmployee().isAvailable(getFlight().getDepartureUTCDate());
    }

    @Override
    public String toString() {
        return flight + "-" + indexInFlight;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public Flight getFlight() {
        return flight;
    }

    public void setFlight(Flight flight) {
        this.flight = flight;
    }

    public int getIndexInFlight() {
        return indexInFlight;
    }

    public void setIndexInFlight(int indexInFlight) {
        this.indexInFlight = indexInFlight;
    }

    public Skill getRequiredSkill() {
        return requiredSkill;
    }

    public void setRequiredSkill(Skill requiredSkill) {
        this.requiredSkill = requiredSkill;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @Override
    public int compareTo(FlightAssignment o) {
        return COMPARATOR.compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FlightAssignment that))
            return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
