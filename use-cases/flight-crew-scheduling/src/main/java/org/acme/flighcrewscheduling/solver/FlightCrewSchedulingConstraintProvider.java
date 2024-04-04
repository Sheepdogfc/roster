package org.acme.flighcrewscheduling.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;
import static ai.timefold.solver.core.api.score.stream.Joiners.lessThan;
import static ai.timefold.solver.core.api.score.stream.Joiners.overlapping;

import ai.timefold.solver.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import org.acme.flighcrewscheduling.domain.Employee;
import org.acme.flighcrewscheduling.domain.FlightAssignment;

public class FlightCrewSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                requiredSkill(constraintFactory),
                flightConflict(constraintFactory),
                transferBetweenTwoFlights(constraintFactory),
                employeeUnavailability(constraintFactory),
                firstAssignmentNotDepartingFromHome(constraintFactory),
                lastAssignmentNotArrivingAtHome(constraintFactory)
        };
    }

    public Constraint requiredSkill(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(FlightAssignment.class)
                .filter(flightAssignment -> !flightAssignment.hasRequiredSkills())
                .penalize(HardSoftLongScore.ofHard(100))
                .asConstraint("Required skill");
    }

    public Constraint flightConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(FlightAssignment.class,
                equal(FlightAssignment::getEmployee),
                overlapping(flightAssignment -> flightAssignment.getFlight().getDepartureUTCDateTime(),
                        flightAssignment -> flightAssignment.getFlight().getArrivalUTCDateTime()))
                .penalize(HardSoftLongScore.ofHard(10))
                .asConstraint("Flight conflict");
    }

    public Constraint transferBetweenTwoFlights(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(FlightAssignment.class)
                .join(FlightAssignment.class, equal(FlightAssignment::getEmployee),
                        lessThan(flightAssignment -> flightAssignment.getFlight().getDepartureUTCDateTime()),
                        filtering((flightAssignment,
                                flightAssignment2) -> !flightAssignment.getId().equals(flightAssignment2.getId())))
                .ifNotExists(FlightAssignment.class,
                        filtering((flightAssignment, flightAssignment2,
                                otherFlightAssignment) -> !otherFlightAssignment.getId().equals(flightAssignment.getId())
                                        && !otherFlightAssignment.getId().equals(flightAssignment2.getId())
                                        && !otherFlightAssignment.getFlight().getDepartureUTCDateTime()
                                                .isBefore(flightAssignment.getFlight().getDepartureUTCDateTime())
                                        && otherFlightAssignment.getEmployee().equals(flightAssignment2.getEmployee())
                                        && otherFlightAssignment.getFlight().getDepartureUTCDateTime()
                                                .isBefore(flightAssignment2.getFlight().getDepartureUTCDateTime())))
                .filter((flightAssignment,
                        flightAssignment2) -> !flightAssignment.getFlight().getArrivalAirport()
                                .equals(flightAssignment2.getFlight().getDepartureAirport()))
                .penalize(HardSoftLongScore.ofHard(1))
                .asConstraint("Transfer between two flights");
    }

    public Constraint employeeUnavailability(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(FlightAssignment.class)
                .filter(FlightAssignment::isUnavailableEmployee)
                .penalize(HardSoftLongScore.ofHard(10))
                .asConstraint("Employee unavailable");
    }

    public Constraint firstAssignmentNotDepartingFromHome(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .join(FlightAssignment.class,
                        filtering((employee, flightAssignment) -> employee.equals(flightAssignment.getEmployee())))
                .ifNotExists(FlightAssignment.class,
                        filtering((employee, flightAssignment,
                                otherFlightAssignment) -> employee.equals(otherFlightAssignment.getEmployee())
                                        && otherFlightAssignment.getFlight().getDepartureUTCDateTime()
                                                .isBefore(flightAssignment.getFlight().getDepartureUTCDateTime())))
                .filter((employee,
                        flightAssignment) -> !employee.getHomeAirport()
                                .equals(flightAssignment.getFlight().getDepartureAirport()))
                .penalize(HardSoftLongScore.ofSoft(1000))
                .asConstraint("First assignment not departing from home");
    }

    public Constraint lastAssignmentNotArrivingAtHome(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Employee.class)
                .join(FlightAssignment.class,
                        filtering((employee, flightAssignment) -> employee.equals(flightAssignment.getEmployee())))
                .ifNotExists(FlightAssignment.class,
                        filtering((employee, flightAssignment,
                                otherFlightAssignment) -> employee.equals(otherFlightAssignment.getEmployee())
                                        && otherFlightAssignment.getFlight().getDepartureUTCDateTime()
                                                .isAfter(flightAssignment.getFlight().getDepartureUTCDateTime())))
                .filter((employee,
                        flightAssignment) -> !employee.getHomeAirport()
                                .equals(flightAssignment.getFlight().getArrivalAirport()))
                .penalize(HardSoftLongScore.ofSoft(1000))
                .asConstraint("Last assignment not arriving at home");
    }

}
