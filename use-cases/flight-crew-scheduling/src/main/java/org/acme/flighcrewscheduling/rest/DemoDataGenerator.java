package org.acme.flighcrewscheduling.rest;

import static java.time.temporal.TemporalAdjusters.firstInMonth;
import static java.util.Collections.unmodifiableList;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.impl.util.MutableInt;
import ai.timefold.solver.core.impl.util.Pair;

import org.acme.flighcrewscheduling.domain.Airport;
import org.acme.flighcrewscheduling.domain.Employee;
import org.acme.flighcrewscheduling.domain.Flight;
import org.acme.flighcrewscheduling.domain.FlightAssignment;
import org.acme.flighcrewscheduling.domain.FlightCrewSchedule;

@ApplicationScoped
public class DemoDataGenerator {

    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Chad", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay",
            "Jeri", "Hope", "Avis", "Lino", "Lyle", "Nick", "Dino", "Otha", "Gwen", "Jose", "Dena", "Jana", "Dave",
            "Russ", "Josh", "Dana", "Katy" };
    private static final String[] LAST_NAMES =
            { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt", "Howe", "Lowe", "Wise", "Clay",
                    "Carr", "Hood", "Long", "Horn", "Haas", "Meza" };
    private static final String ATTENDANT_SKILL = "Flight attendant";
    private static final String PILOT_SKILL = "Pilot";
    private static final Random random = new Random(0);

    public FlightCrewSchedule generateDemoData() {
        FlightCrewSchedule schedule = new FlightCrewSchedule();
        // Airports
        List<Airport> airports = List.of(
                new Airport("BRU", "BRU", 50.901389, 4.484444),
                new Airport("LHR", "LHR", 51.4775, -0.461389),
                new Airport("CDG", "CDG", 49.009722, 2.547778),
                new Airport("AMS", "AMS", 52.308056, 4.764167),
                new Airport("FRA", "FRA", 50.033333, 8.570556));

        // Flights
        LocalDate firstMonthMonday = LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)); // First Monday of the month
        int countDays = 7;
        List<LocalDate> dates = new ArrayList<>(countDays);
        dates.add(firstMonthMonday);
        for (int i = 1; i < countDays; i++) {
            dates.add(firstMonthMonday.with(firstInMonth(DayOfWeek.MONDAY)).plusDays(i));
        }
        List<Airport> homeAirports = new ArrayList(2);
        homeAirports.add(pickRandomAirport(airports, ""));
        homeAirports.add(pickRandomAirport(airports, homeAirports.get(0).getCode()));
        List<LocalTime> times = IntStream.range(6, 20)
                .mapToObj(i -> LocalTime.of(i, 0))
                .toList();
        int countFlights = 50;
        List<Flight> flights = generateFlights(countFlights, airports, homeAirports, dates, times);

        // Flight assignments
        List<FlightAssignment> flightAssignments = generateFlightAssignments(flights);

        // Employees
        List<Employee> employees = generateEmployees(flights, dates);

        // Update problem facts
        schedule.setAirports(airports);
        schedule.setEmployees(employees);
        schedule.setFlights(flights);
        schedule.setFlightAssignments(flightAssignments);

        return schedule;
    }

    private List<Employee> generateEmployees(List<Flight> flights, List<LocalDate> dates) {
        Supplier<String> nameSupplier = () -> {
            Function<String[], String> randomStringSelector = strings -> strings[random.nextInt(strings.length)];
            String firstName = randomStringSelector.apply(FIRST_NAMES);
            String lastName = randomStringSelector.apply(LAST_NAMES);
            return firstName + " " + lastName;
        };

        List<Airport> flightAirports = flights.stream()
                .map(Flight::getDepartureAirport)
                .distinct()
                .toList();

        // two pilots and three attendants per airport
        List<Employee> employees = new ArrayList<>(flightAirports.size() * 5);

        MutableInt count = new MutableInt();
        // Three teams per airport
        flightAirports.forEach(airport -> IntStream.range(0, 3).forEach(i -> {
            employees.add(new Employee(String.valueOf(count.increment()), nameSupplier.get(), airport, List.of(PILOT_SKILL)));
            employees.add(new Employee(String.valueOf(count.increment()), nameSupplier.get(), airport, List.of(PILOT_SKILL)));
            employees.add(
                    new Employee(String.valueOf(count.increment()), nameSupplier.get(), airport, List.of(ATTENDANT_SKILL)));
            employees.add(
                    new Employee(String.valueOf(count.increment()), nameSupplier.get(), airport, List.of(ATTENDANT_SKILL)));
        }));

        // Unavailable dates - 28% one date; 4% two dates
        applyRandomValue((int) (0.28 * employees.size()), employees, e -> e.getUnavailableDays() == null,
                e -> e.setUnavailableDays(List.of(dates.get(random.nextInt(dates.size())))));
        applyRandomValue((int) (0.04 * employees.size()), employees, e -> e.getUnavailableDays() == null,
                e -> {
                    List<LocalDate> unavailableDates = new ArrayList<>(2);
                    while (unavailableDates.size() < 2) {
                        LocalDate nextDate = dates.get(random.nextInt(dates.size()));
                        if (!unavailableDates.contains(nextDate)) {
                            unavailableDates.add(nextDate);
                        }
                    }
                    e.setUnavailableDays(unmodifiableList(unavailableDates));
                });

        return employees;
    }

    private List<Flight> generateFlights(int size, List<Airport> airports, List<Airport> homeAirports,
            List<LocalDate> dates, List<LocalTime> timeGroups) {
        if (size % 2 != 0) {
            throw new IllegalArgumentException("The size of flights must be even");
        }

        // Departure and arrival airports
        List<Flight> flights = new ArrayList<>(size);
        List<Airport> remainingAirports = airports.stream()
                .filter(airport -> !homeAirports.contains(airport))
                .toList();
        int countFlights = 0;
        while (countFlights < size) {
            int routeSize = pickRandomRouteSize(countFlights, size);
            Airport homeAirport = homeAirports.get(random.nextInt(homeAirports.size()));
            Flight homeFlight = new Flight(String.valueOf(countFlights++), homeAirport,
                    remainingAirports.get(random.nextInt(remainingAirports.size())));
            flights.add(homeFlight);
            Flight nextFlight = homeFlight;
            for (int i = 0; i < routeSize - 2; i++) {
                nextFlight = new Flight(String.valueOf(countFlights++), nextFlight.getArrivalAirport(),
                        pickRandomAirport(remainingAirports, nextFlight.getArrivalAirport().getCode()));
                flights.add(nextFlight);
            }
            flights.add(new Flight(String.valueOf(countFlights++), nextFlight.getArrivalAirport(),
                    homeFlight.getDepartureAirport()));
        }

        // Flight number
        IntStream.range(0, flights.size()).forEach(i -> flights.get(i)
                .setFlightNumber("%s%s".formatted(flights.get(i).getDepartureAirport().getCode(), String.valueOf(i))));

        // Flight duration - 1h 16%; 2h 32%; 3h 48%; 4h 4%
        List<Pair<Float, Integer>> timeCount = List.of(
                new Pair<>(0.16f, 1),
                new Pair<>(0.48f, 2),
                new Pair<>(0.96f, 3),
                new Pair<>(1f, 4));
        int countDates = size / dates.size();
        BiConsumer<Flight, LocalDate> flightConsumer = (flight, date) -> {
            double nextCountHours = random.nextDouble();
            int countHours = timeCount.stream()
                    .filter(p -> nextCountHours <= p.key())
                    .mapToInt(Pair::value)
                    .findFirst()
                    .getAsInt();
            LocalTime startTime = timeGroups.get(random.nextInt(timeGroups.size() - countHours));
            LocalDateTime departureDateTime = LocalDateTime.of(date, startTime);
            LocalDateTime arrivalDateTime = LocalDateTime.of(date, startTime.plusHours(countHours));
            flight.setDepartureUTCDateTime(departureDateTime);
            flight.setArrivalUTCDateTime(arrivalDateTime);
        };
        dates.forEach(startDate -> applyRandomValue(countDates, flights, startDate,
                flight -> flight.getDepartureUTCDateTime() == null, flightConsumer));
        // Ensure there are no empty dates
        flights.stream()
                .filter(flight -> flight.getDepartureUTCDateTime() == null)
                .forEach(flight -> flightConsumer.accept(flight, dates.get(random.nextInt(dates.size()))));
        return flights;
    }

    private Airport pickRandomAirport(List<Airport> airports, String excludeCode) {
        Airport airport = null;
        while (airport == null || airport.getCode().equals(excludeCode)) {
            airport = airports.stream()
                    .skip(random.nextInt(airports.size()))
                    .findFirst()
                    .get();
        }
        return airport;
    }

    private int pickRandomRouteSize(int countFlights, int maxCountFlights) {
        List<Integer> allowedSizes = List.of(2, 4, 6);
        int limit = maxCountFlights - countFlights;
        int routeSize = 0;
        while (routeSize == 0 || routeSize > limit) {
            routeSize = allowedSizes.stream()
                    .skip(random.nextInt(3))
                    .findFirst()
                    .get();
        }
        return routeSize;
    }

    private List<FlightAssignment> generateFlightAssignments(List<Flight> flights) {
        // 2 pilots and 3 attendants
        List<FlightAssignment> flightAssignments = new ArrayList<>(flights.size() * 5);
        MutableInt count = new MutableInt();
        flights.forEach(flight -> {
            MutableInt indexSkill = new MutableInt();
            flightAssignments
                    .add(new FlightAssignment(String.valueOf(count.increment()), flight, indexSkill.increment(), PILOT_SKILL));
            flightAssignments
                    .add(new FlightAssignment(String.valueOf(count.increment()), flight, indexSkill.increment(), PILOT_SKILL));
            flightAssignments
                    .add(new FlightAssignment(String.valueOf(count.increment()), flight, indexSkill.increment(),
                            ATTENDANT_SKILL));
            flightAssignments
                    .add(new FlightAssignment(String.valueOf(count.increment()), flight, indexSkill.increment(),
                            ATTENDANT_SKILL));
        });
        return flightAssignments;
    }

    private <T> void applyRandomValue(int count, List<T> values, Predicate<T> filter, Consumer<T> consumer) {
        int size = (int) values.stream().filter(filter).count();
        for (int i = 0; i < count; i++) {
            values.stream()
                    .filter(filter)
                    .skip(size > 0 ? random.nextInt(size) : 0).findFirst()
                    .ifPresent(consumer::accept);
            size--;
            if (size < 0) {
                break;
            }
        }
    }

    private <T, L> void applyRandomValue(int count, List<T> values, L secondParam, Predicate<T> filter,
            BiConsumer<T, L> consumer) {
        int size = (int) values.stream().filter(filter).count();
        for (int i = 0; i < count; i++) {
            values.stream()
                    .filter(filter)
                    .skip(size > 0 ? random.nextInt(size) : 0).findFirst()
                    .ifPresent(v -> consumer.accept(v, secondParam));
            size--;
            if (size < 0) {
                break;
            }
        }
    }
}
