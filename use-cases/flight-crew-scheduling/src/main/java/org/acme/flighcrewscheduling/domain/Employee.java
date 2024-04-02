package org.acme.flighcrewscheduling.domain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@PlanningEntity
@JsonIdentityInfo(scope = Employee.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Employee {

    @PlanningId
    private String id;
    private String name;
    private Airport homeAirport;

    private List<String> skills;
    private List<LocalDate> unavailableDays;

    @JsonIgnore
    @InverseRelationShadowVariable(sourceVariableName = "employee")
    private SortedSet<FlightAssignment> flightAssignments;

    public Employee() {
        this.flightAssignments = new TreeSet<>();
        this.unavailableDays = new ArrayList<>();
    }

    public Employee(String id) {
        this.id = id;
        this.flightAssignments = new TreeSet<>();
        this.unavailableDays = new ArrayList<>();
    }

    public Employee(String id, String name) {
        this.id = id;
        this.name = name;
        this.flightAssignments = new TreeSet<>();
        this.unavailableDays = new ArrayList<>();
    }

    public Employee(String id, String name, Airport homeAirport, List<String> skills) {
        this.id = id;
        this.name = name;
        this.homeAirport = homeAirport;
        this.skills = skills;
    }

    @JsonIgnore
    public boolean hasSkill(String skill) {
        return skills.contains(skill);
    }

    @JsonIgnore
    public boolean isAvailable(LocalDate date) {
        return unavailableDays == null || !unavailableDays.contains(date);
    }

    @JsonIgnore
    public boolean isFirstAssignmentDepartingFromHome() {
        if (flightAssignments.isEmpty()) {
            return true;
        }
        FlightAssignment firstAssignment = flightAssignments.first();
        // TODO allow taking a taxi, but penalize it with a soft score instead
        return firstAssignment.getFlight().getDepartureAirport() == homeAirport;
    }

    @JsonIgnore
    public boolean isLastAssignmentArrivingAtHome() {
        if (flightAssignments.isEmpty()) {
            return true;
        }
        FlightAssignment lastAssignment = flightAssignments.last();
        // TODO allow taking a taxi, but penalize it with a soft score instead
        return lastAssignment.getFlight().getArrivalAirport() == homeAirport;
    }

    @JsonIgnore
    public long countInvalidConnections() {
        long count = 0L;
        FlightAssignment previousAssignment = null;
        for (FlightAssignment assignment : flightAssignments) {
            if (previousAssignment != null
                    && !previousAssignment.getFlight().getArrivalAirport()
                            .equals(assignment.getFlight().getDepartureAirport())) {
                count++;
            }
            previousAssignment = assignment;
        }
        return count;
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

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public List<LocalDate> getUnavailableDays() {
        return unavailableDays;
    }

    public void setUnavailableDays(List<LocalDate> unavailableDays) {
        this.unavailableDays = unavailableDays;
    }

    public SortedSet<FlightAssignment> getFlightAssignments() {
        return flightAssignments;
    }

    public void setFlightAssignments(SortedSet<FlightAssignment> flightAssignments) {
        if (flightAssignments == null) {
            this.flightAssignments = new TreeSet<>();
        } else {
            this.flightAssignments = flightAssignments;
        }
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
