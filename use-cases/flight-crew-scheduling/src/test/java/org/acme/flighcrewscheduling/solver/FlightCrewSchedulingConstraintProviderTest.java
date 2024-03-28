package org.acme.flighcrewscheduling.solver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.flighcrewscheduling.domain.Airport;
import org.acme.flighcrewscheduling.domain.Employee;
import org.acme.flighcrewscheduling.domain.Flight;
import org.acme.flighcrewscheduling.domain.FlightAssignment;
import org.acme.flighcrewscheduling.domain.FlightCrewSchedule;
import org.acme.flighcrewscheduling.domain.Skill;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class FlightCrewSchedulingConstraintProviderTest {

    private final ConstraintVerifier<FlightCrewSchedulingConstraintProvider, FlightCrewSchedule> constraintVerifier;

    @Inject
    public FlightCrewSchedulingConstraintProviderTest(
            ConstraintVerifier<FlightCrewSchedulingConstraintProvider, FlightCrewSchedule> constraintVerifier) {
        this.constraintVerifier = constraintVerifier;
    }

    @Test
    void requiredSkill() {
        FlightAssignment assignment = new FlightAssignment("1", null, 0, new Skill("1"));
        Employee employee = new Employee("1");
        employee.setSkills(Set.of(new Skill("2")));
        assignment.setEmployee(employee);

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::requiredSkill)
                .given(assignment)
                .penalizesBy(1); // missing requiredSkill
    }

    @Test
    void flightConflict() {
        Employee employee = new Employee("1");

        Flight flight = new Flight("1", "1", null, LocalDateTime.now(), null, LocalDateTime.now().plusMinutes(10));
        FlightAssignment assignment = new FlightAssignment("1", flight);
        assignment.setEmployee(employee);

        Flight overlappingFlight =
                new Flight("1", "1", null, LocalDateTime.now().plusMinutes(1), null, LocalDateTime.now().plusMinutes(11));
        FlightAssignment overlappingAssignment = new FlightAssignment("2", overlappingFlight);
        overlappingAssignment.setEmployee(employee);

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::flightConflict)
                .given(assignment, overlappingAssignment)
                .penalizesBy(1); // one overlapping firstFlight
    }

    @Test
    void transferBetweenTwoFlights() {
        Employee employee = new Employee("1");

        Airport firstAirport = new Airport("1", "1");
        Airport secondAirport = new Airport("2", "2");

        Flight firstFlight =
                new Flight("1", "1", firstAirport, LocalDateTime.now(), secondAirport, LocalDateTime.now().plusMinutes(10));
        FlightAssignment firstAssignment = new FlightAssignment("1", firstFlight);

        Flight firstInvalidFlight =
                new Flight("2", "2", firstAirport, LocalDateTime.now(), secondAirport, LocalDateTime.now().plusMinutes(10));
        FlightAssignment firstInvalidAssignment = new FlightAssignment("2", firstInvalidFlight);

        Flight secondInvalidFlight =
                new Flight("3", "3", firstAirport, LocalDateTime.now(), secondAirport, LocalDateTime.now().plusMinutes(10));
        FlightAssignment secondInvalidAssignment = new FlightAssignment("3", secondInvalidFlight);

        SortedSet<FlightAssignment> assignments = new TreeSet<>();
        assignments.add(firstAssignment);
        assignments.add(firstInvalidAssignment);
        assignments.add(secondInvalidAssignment);
        employee.setFlightAssignments(assignments);

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::transferBetweenTwoFlights)
                .given(employee)
                .penalizesBy(2); // two invalid connections
    }

    @Test
    void employeeUnavailability() {
        Employee employee = new Employee("1");
        employee.setUnavailableDays(Set.of(LocalDate.now()));

        Flight flight =
                new Flight("1", "1", null, LocalDateTime.now(), null, LocalDateTime.now().plusMinutes(10));
        FlightAssignment assignment = new FlightAssignment("1", flight);
        assignment.setEmployee(employee);

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::employeeUnavailability)
                .given(assignment)
                .penalizesBy(1); // one unavailable date
    }

    @Test
    void firstAssignmentNotDepartingFromHome() {
        Employee employee = new Employee("1");
        employee.setHomeAirport(new Airport("1", "1"));
        employee.setUnavailableDays(Set.of(LocalDate.now()));

        Flight flight =
                new Flight("1", "1", new Airport("2", "2"), LocalDateTime.now(), new Airport("3", "3"),
                        LocalDateTime.now().plusMinutes(10));
        FlightAssignment assignment = new FlightAssignment("1", flight);

        SortedSet<FlightAssignment> assignments = new TreeSet<>();
        assignments.add(assignment);
        employee.setFlightAssignments(assignments);

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::firstAssignmentNotDepartingFromHome)
                .given(employee)
                .penalizesBy(1); // invalid first airport
    }

    @Test
    void lastAssignmentNotArrivingAtHome() {
        Employee employee = new Employee("1");
        employee.setHomeAirport(new Airport("1", "1"));
        employee.setUnavailableDays(Set.of(LocalDate.now()));

        Flight firstFlight =
                new Flight("1", "1", new Airport("2", "2"), LocalDateTime.now(), new Airport("3", "3"),
                        LocalDateTime.now().plusMinutes(10));
        FlightAssignment firstAssignment = new FlightAssignment("1", firstFlight);

        Flight secondFlight =
                new Flight("2", "2", new Airport("4", "4"), LocalDateTime.now(), new Airport("4", "4"),
                        LocalDateTime.now().plusMinutes(10));
        FlightAssignment secondAssignment = new FlightAssignment("1", secondFlight);

        SortedSet<FlightAssignment> assignments = new TreeSet<>();
        assignments.add(secondAssignment);
        assignments.add(secondAssignment);
        employee.setFlightAssignments(assignments);

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::lastAssignmentNotArrivingAtHome)
                .given(employee)
                .penalizesBy(1); // invalid last airport
    }
}
