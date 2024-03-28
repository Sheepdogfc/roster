package org.acme.flighcrewscheduling.domain;

import java.util.Objects;

public class FlightCrewParametrization {

    public static final String REQUIRED_SKILL = "Required skill";
    public static final String FLIGHT_CONFLICT = "Flight conflict";
    public static final String TRANSFER_BETWEEN_TWO_FLIGHTS = "Transfer between two flights";
    public static final String EMPLOYEE_UNAVAILABILITY = "Employee unavailability";

    public static final String LOAD_BALANCE_FLIGHT_DURATION_TOTAL_PER_EMPLOYEE =
            "Load balance flight duration total per employee";

    private String id;
    private long loadBalanceFlightDurationTotalPerEmployee = 1;

    public FlightCrewParametrization() {
    }

    public FlightCrewParametrization(String id) {
        this.id = id;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public long getLoadBalanceFlightDurationTotalPerEmployee() {
        return loadBalanceFlightDurationTotalPerEmployee;
    }

    public void setLoadBalanceFlightDurationTotalPerEmployee(long loadBalanceFlightDurationTotalPerEmployee) {
        this.loadBalanceFlightDurationTotalPerEmployee = loadBalanceFlightDurationTotalPerEmployee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FlightCrewParametrization that))
            return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
