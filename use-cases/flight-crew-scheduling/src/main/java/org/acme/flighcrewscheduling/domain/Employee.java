package org.acme.flighcrewscheduling.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;

@PlanningEntity
public class Employee {

    @PlanningId
    private String id;
    private String name;
    private Airport homeAirport;

    private Set<Skill> skills;
    private Set<LocalDate> unavailableDays;

    @InverseRelationShadowVariable(sourceVariableName = "employee")
    private SortedSet<FlightAssignment> flightAssignments;

    public Employee() {
    }

    public Employee(String id) {
        this.id = id;
    }

    public Employee(String id, String name, Airport homeAirport) {
        this.id = id;
        this.name = name;
        this.homeAirport = homeAirport;
    }

    public boolean hasSkill(Skill skill) {
        return skills.contains(skill);
    }

    public boolean isAvailable(LocalDate date) {
        return !unavailableDays.contains(date);
    }

    public boolean isFirstAssignmentDepartingFromHome() {
        if (flightAssignments.isEmpty()) {
            return true;
        }
        FlightAssignment firstAssignment = flightAssignments.first();
        // TODO allow taking a taxi, but penalize it with a soft score instead
        return firstAssignment.getFlight().getDepartureAirport() == homeAirport;
    }

    public boolean isLastAssignmentArrivingAtHome() {
        if (flightAssignments.isEmpty()) {
            return true;
        }
        FlightAssignment lastAssignment = flightAssignments.last();
        // TODO allow taking a taxi, but penalize it with a soft score instead
        return lastAssignment.getFlight().getArrivalAirport() == homeAirport;
    }

    public long countInvalidConnections() {
        long count = 0L;
        FlightAssignment previousAssignment = null;
        for (FlightAssignment assignment : flightAssignments) {
            if (previousAssignment != null
                    && previousAssignment.getFlight().getArrivalAirport() != assignment.getFlight().getDepartureAirport()) {
                count++;
            }
            previousAssignment = assignment;
        }
        return count;
    }

    public long getFlightDurationTotalInMinutes() {
        long total = 0L;
        for (FlightAssignment flightAssignment : flightAssignments) {
            total += flightAssignment.getFlightDurationInMinutes();
        }
        return total;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Airport getHomeAirport() {
        return homeAirport;
    }

    public void setHomeAirport(Airport homeAirport) {
        this.homeAirport = homeAirport;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public void setSkills(Set<Skill> skills) {
        this.skills = skills;
    }

    public Set<LocalDate> getUnavailableDays() {
        return unavailableDays;
    }

    public void setUnavailableDays(Set<LocalDate> unavailableDays) {
        this.unavailableDays = unavailableDays;
    }

    public SortedSet<FlightAssignment> getFlightAssignments() {
        return flightAssignments;
    }

    public void setFlightAssignments(SortedSet<FlightAssignment> flightAssignments) {
        this.flightAssignments = flightAssignments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Employee employee))
            return false;
        return Objects.equals(getId(), employee.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
