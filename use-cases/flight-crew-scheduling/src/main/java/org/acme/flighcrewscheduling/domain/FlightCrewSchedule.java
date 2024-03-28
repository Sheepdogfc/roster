package org.acme.flighcrewscheduling.domain;

import java.time.LocalDate;
import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.ProblemFactProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

@PlanningSolution
public class FlightCrewSchedule {

    private LocalDate scheduleFirstUTCDate;
    private LocalDate scheduleLastUTCDate;

    @ProblemFactProperty
    private FlightCrewParametrization parametrization;

    @ProblemFactCollectionProperty
    private List<Skill> skills;

    @ProblemFactCollectionProperty
    private List<Airport> airports;

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Employee> employees;

    @ProblemFactCollectionProperty
    private List<Flight> flights;

    @PlanningEntityCollectionProperty
    private List<FlightAssignment> flightAssignments;

    @PlanningScore
    private HardSoftLongScore score = null;

    public FlightCrewSchedule() {
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public LocalDate getScheduleFirstUTCDate() {
        return scheduleFirstUTCDate;
    }

    public void setScheduleFirstUTCDate(LocalDate scheduleFirstUTCDate) {
        this.scheduleFirstUTCDate = scheduleFirstUTCDate;
    }

    public LocalDate getScheduleLastUTCDate() {
        return scheduleLastUTCDate;
    }

    public void setScheduleLastUTCDate(LocalDate scheduleLastUTCDate) {
        this.scheduleLastUTCDate = scheduleLastUTCDate;
    }

    public FlightCrewParametrization getParametrization() {
        return parametrization;
    }

    public void setParametrization(FlightCrewParametrization parametrization) {
        this.parametrization = parametrization;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Airport> getAirports() {
        return airports;
    }

    public void setAirports(List<Airport> airports) {
        this.airports = airports;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }

    public List<FlightAssignment> getFlightAssignments() {
        return flightAssignments;
    }

    public void setFlightAssignments(List<FlightAssignment> flightAssignments) {
        this.flightAssignments = flightAssignments;
    }

    public HardSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardSoftLongScore score) {
        this.score = score;
    }

}
