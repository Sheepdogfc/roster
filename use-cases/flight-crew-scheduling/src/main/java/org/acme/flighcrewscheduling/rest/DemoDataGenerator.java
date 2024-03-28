package org.acme.flighcrewscheduling.rest;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.flighcrewscheduling.domain.Airport;
import org.acme.flighcrewscheduling.domain.FlightCrewSchedule;

@ApplicationScoped
public class DemoDataGenerator {

    public FlightCrewSchedule generateDemoData() {
        FlightCrewSchedule schedule = new FlightCrewSchedule();
        List<Airport> airports = List.of(
                new Airport("BRU", "BRU", 50.901389, 4.484444),
                new Airport("LHR", "LHR", 51.4775, -0.461389),
                new Airport("CDG", "CDG", 49.009722, 2.547778),
                new Airport("AMS", "AMS", 52.308056, 4.764167),
                new Airport("FRA", "FRA", 50.033333, 8.570556),
                new Airport("IST", "IST", 40.976111, 28.814167),
                new Airport("MAD", "MAD", 40.472222, -3.560833),
                new Airport("BCN", "BCN", 41.296944, 2.078333),
                new Airport("LGW", "LGW", 51.148056, -0.190278)
        );
        schedule.setAirports(airports);

        return schedule;
    }
}
