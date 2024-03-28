package org.acme.flighcrewscheduling.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class Flight implements Comparable<Flight> {

    private static final Comparator<Flight> COMPARATOR = Comparator.comparing(Flight::getDepartureUTCDateTime)
            .thenComparing(Flight::getDepartureAirport)
            .thenComparing(Flight::getArrivalUTCDateTime)
            .thenComparing(Flight::getArrivalAirport)
            .thenComparing(Flight::getFlightNumber);

    @PlanningId
    private String id;
    private String flightNumber;
    private Airport departureAirport;
    private LocalDateTime departureUTCDateTime;
    private Airport arrivalAirport;
    private LocalDateTime arrivalUTCDateTime;

    public Flight() {
    }

    public Flight(String id, String flightNumber, Airport departureAirport, LocalDateTime departureUTCDateTime,
            Airport arrivalAirport, LocalDateTime arrivalUTCDateTime) {
        this.id = id;
        this.flightNumber = flightNumber;
        this.departureAirport = departureAirport;
        this.departureUTCDateTime = departureUTCDateTime;
        this.arrivalAirport = arrivalAirport;
        this.arrivalUTCDateTime = arrivalUTCDateTime;
    }

    public long getDurationInMinutes() {
        return ChronoUnit.MINUTES.between(departureUTCDateTime, arrivalUTCDateTime);
    }

    public LocalDate getDepartureUTCDate() {
        return departureUTCDateTime.toLocalDate();
    }

    @Override
    public String toString() {
        return flightNumber + "@" + departureUTCDateTime.toLocalDate();
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public Airport getDepartureAirport() {
        return departureAirport;
    }

    public void setDepartureAirport(Airport departureAirport) {
        this.departureAirport = departureAirport;
    }

    public LocalDateTime getDepartureUTCDateTime() {
        return departureUTCDateTime;
    }

    public void setDepartureUTCDateTime(LocalDateTime departureUTCDateTime) {
        this.departureUTCDateTime = departureUTCDateTime;
    }

    public Airport getArrivalAirport() {
        return arrivalAirport;
    }

    public void setArrivalAirport(Airport arrivalAirport) {
        this.arrivalAirport = arrivalAirport;
    }

    public LocalDateTime getArrivalUTCDateTime() {
        return arrivalUTCDateTime;
    }

    public void setArrivalUTCDateTime(LocalDateTime arrivalUTCDateTime) {
        this.arrivalUTCDateTime = arrivalUTCDateTime;
    }

    @Override
    public int compareTo(Flight o) {
        return COMPARATOR.compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Flight flight))
            return false;
        return Objects.equals(getId(), flight.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
