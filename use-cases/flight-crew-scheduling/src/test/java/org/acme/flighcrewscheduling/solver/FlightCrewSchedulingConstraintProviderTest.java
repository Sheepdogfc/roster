package org.acme.flighcrewscheduling.solver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.flighcrewscheduling.domain.Airport;
import org.acme.flighcrewscheduling.domain.Employee;
import org.acme.flighcrewscheduling.domain.Flight;
import org.acme.flighcrewscheduling.domain.FlightAssignment;
import org.acme.flighcrewscheduling.domain.FlightCrewSchedule;
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
        FlightAssignment assignment = new FlightAssignment("1", null, 0, "1");
        Employee employee = new Employee("1");
        employee.setSkills(List.of("2"));
        assignment.setEmployee(employee);

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::requiredSkill)
                .given(assignment)
                .penalizesBy(1); // missing requiredSkill
    }

    @Test
    void flightConflict() {
        Employee employee = new Employee("1");

        Flight flight = new Flight("1", null, LocalDateTime.now(), null, LocalDateTime.now().plusMinutes(10));
        FlightAssignment assignment = new FlightAssignment("1", flight);
        assignment.setEmployee(employee);

        Flight overlappingFlight =
                new Flight("1", null, LocalDateTime.now().plusMinutes(1), null, LocalDateTime.now().plusMinutes(11));
        FlightAssignment overlappingAssignment = new FlightAssignment("2", overlappingFlight);
        overlappingAssignment.setEmployee(employee);

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::flightConflict)
                .given(assignment, overlappingAssignment)
                .penalizesBy(1); // one overlapping thirdFlight
    }

    @Test
    void transferBetweenTwoFlights() {
        Employee employee = new Employee("1");

        Airport firstAirport = new Airport("1");
        Airport secondAirport = new Airport("2");

        Flight firstFlight =
                new Flight("1", firstAirport, LocalDateTime.now(), secondAirport, LocalDateTime.now().plusMinutes(10));
        FlightAssignment firstAssignment = new FlightAssignment("1", firstFlight);
        firstAssignment.setEmployee(employee);

        Flight firstInvalidFlight =
                new Flight("2", firstAirport, LocalDateTime.now().plusMinutes(11), secondAirport, LocalDateTime.now().plusMinutes(12));
        FlightAssignment firstInvalidAssignment = new FlightAssignment("2", firstInvalidFlight);
        firstInvalidAssignment.setEmployee(employee);

        Flight secondInvalidFlight =
                new Flight("3", firstAirport, LocalDateTime.now().plusMinutes(13), secondAirport, LocalDateTime.now().plusMinutes(14));
        FlightAssignment secondInvalidAssignment = new FlightAssignment("3", secondInvalidFlight);
        secondInvalidAssignment.setEmployee(employee);

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::transferBetweenTwoFlights)
                .given(firstAssignment, firstInvalidAssignment, secondInvalidAssignment)
                .penalizesBy(2); // two invalid connections
    }

    @Test
    void employeeUnavailability() {
        Employee employee = new Employee("1");
        employee.setUnavailableDays(List.of(LocalDate.now()));

        Flight flight =
                new Flight("1", null, LocalDateTime.now(), null, LocalDateTime.now().plusMinutes(10));
        FlightAssignment assignment = new FlightAssignment("1", flight);
        assignment.setEmployee(employee);

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::employeeUnavailability)
                .given(assignment)
                .penalizesBy(1); // one unavailable date
    }

    @Test
    void firstAssignmentNotDepartingFromHome() {
        Employee employee = new Employee("1");
        employee.setHomeAirport(new Airport("1"));
        employee.setUnavailableDays(List.of(LocalDate.now()));

        Flight flight =
                new Flight("1", new Airport("2"), LocalDateTime.now(), new Airport("3"),
                        LocalDateTime.now().plusMinutes(10));
        FlightAssignment assignment = new FlightAssignment("1", flight);
        assignment.setEmployee(employee);

        Employee employee2 = new Employee("2");
        employee2.setHomeAirport(new Airport("3"));
        employee2.setUnavailableDays(List.of(LocalDate.now()));

        Flight flight2 =
                new Flight("2", new Airport("3"), LocalDateTime.now(), new Airport("4"),
                        LocalDateTime.now().plusMinutes(10));
        FlightAssignment assignment2 = new FlightAssignment("2", flight2);
        assignment2.setEmployee(employee2);

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::firstAssignmentNotDepartingFromHome)
                .given(employee, employee2, assignment, assignment2)
                .penalizesBy(1); // invalid first airport
    }

    @Test
    void lastAssignmentNotArrivingAtHome() {
        Employee employee = new Employee("1");
        employee.setHomeAirport(new Airport("1"));
        employee.setUnavailableDays(List.of(LocalDate.now()));

        Flight firstFlight =
                new Flight("1", new Airport("2"), LocalDateTime.now(), new Airport("3"),
                        LocalDateTime.now().plusMinutes(10));
        FlightAssignment firstAssignment = new FlightAssignment("1", firstFlight);
        firstAssignment.setEmployee(employee);

        Flight secondFlight =
                new Flight("2", new Airport("3"), LocalDateTime.now().plusMinutes(11), new Airport("4"),
                        LocalDateTime.now().plusMinutes(12));
        FlightAssignment secondAssignment = new FlightAssignment("2", secondFlight);
        secondAssignment.setEmployee(employee);

        Employee employee2 = new Employee("2");
        employee2.setHomeAirport(new Airport("2"));
        employee2.setUnavailableDays(List.of(LocalDate.now()));

        Flight thirdFlight =
                new Flight("3", new Airport("2"), LocalDateTime.now(), new Airport("3"),
                        LocalDateTime.now().plusMinutes(10));
        FlightAssignment thirdFlightAssignment = new FlightAssignment("3", thirdFlight);
        thirdFlightAssignment.setEmployee(employee2);

        Flight fourthFlight =
                new Flight("4", new Airport("3"), LocalDateTime.now().plusMinutes(11), new Airport("2"),
                        LocalDateTime.now().plusMinutes(12));
        FlightAssignment fourthFlightAssignment = new FlightAssignment("4", fourthFlight);
        fourthFlightAssignment.setEmployee(employee2);

        constraintVerifier.verifyThat(FlightCrewSchedulingConstraintProvider::lastAssignmentNotArrivingAtHome)
                .given(employee, employee2, firstAssignment, secondAssignment, thirdFlightAssignment, fourthFlightAssignment)
                .penalizesBy(1); // invalid last airport
    }
}
