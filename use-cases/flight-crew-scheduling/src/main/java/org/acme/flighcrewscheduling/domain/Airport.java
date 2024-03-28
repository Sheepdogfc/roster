package org.acme.flighcrewscheduling.domain;

import java.util.Map;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class Airport implements Comparable<Airport> {

    @PlanningId
    private String id;
    private String code; // IATA 3-letter code
    private String name;

    private double latitude;
    private double longitude;

    private Map<Airport, Long> taxiTimeInMinutes;

    public Airport() {
    }

    public Airport(String id, String code) {
        this.id = id;
        this.code = code;
    }

    public Airport(String id, String code, String name, double latitude, double longitude) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * @param other never null
     * @return null if no taxi connection
     */
    public Long getTaxiTimeInMinutesTo(Airport other) {
        return taxiTimeInMinutes.get(other);
    }

    public double getHaversineDistanceInKmTo(Airport other) {
        if (this == other) {
            return 0.0;
        }
        final int EARTH_RADIUS_IN_KM = 6371;
        final int TWICE_EARTH_RADIUS_IN_KM = 2 * EARTH_RADIUS_IN_KM;

        double latitudeInRads = Math.toRadians(latitude);
        double longitudeInRads = Math.toRadians(longitude);
        // Cartesian coordinates, normalized for a sphere of diameter 1.0
        double cartesianX = 0.5 * Math.cos(latitudeInRads) * Math.sin(longitudeInRads);
        double cartesianY = 0.5 * Math.cos(latitudeInRads) * Math.cos(longitudeInRads);
        double cartesianZ = 0.5 * Math.sin(latitudeInRads);

        double otherLatitudeInRads = Math.toRadians(other.latitude);
        double otherLongitudeInRads = Math.toRadians(other.longitude);
        // Cartesian coordinates, normalized for a sphere of diameter 1.0
        double otherCartesianX = 0.5 * Math.cos(otherLatitudeInRads) * Math.sin(otherLongitudeInRads);
        double otherCartesianY = 0.5 * Math.cos(otherLatitudeInRads) * Math.cos(otherLongitudeInRads);
        double otherCartesianZ = 0.5 * Math.sin(otherLatitudeInRads);

        // TODO cache the part above
        double dX = cartesianX - otherCartesianX;
        double dY = cartesianY - otherCartesianY;
        double dZ = cartesianZ - otherCartesianZ;
        double r = Math.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
        return TWICE_EARTH_RADIUS_IN_KM * Math.asin(r);
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Map<Airport, Long> getTaxiTimeInMinutes() {
        return taxiTimeInMinutes;
    }

    public void setTaxiTimeInMinutes(Map<Airport, Long> taxiTimeInMinutes) {
        this.taxiTimeInMinutes = taxiTimeInMinutes;
    }

    @Override
    public int compareTo(Airport o) {
        return code.compareTo(o.code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Airport airport))
            return false;
        return Objects.equals(id, airport.id);
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
