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

    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Chad", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay" };
    private static final String[] LAST_NAMES = { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt" };
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
                new Airport("FRA", "FRA", 50.033333, 8.570556),
                new Airport("IST", "IST", 40.976111, 28.814167),
                new Airport("MAD", "MAD", 40.472222, -3.560833),
                new Airport("BCN", "BCN", 41.296944, 2.078333),
                new Airport("LGW", "LGW", 51.148056, -0.190278));

        // Employees
        LocalDate firstMonthMonday = LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)); // First Monday of the month
        int countDays = 7;
        List<LocalDate> dates = new ArrayList<>(countDays);
        dates.add(firstMonthMonday);
        for (int i = 1; i < countDays; i++) {
            dates.add(firstMonthMonday.with(firstInMonth(DayOfWeek.MONDAY)).plusDays(i));
        }
        List<Employee> employees = generateEmployees(100, airports, dates);

        // Flights
        List<LocalTime> times = IntStream.range(6, 20)
                .mapToObj(i -> LocalTime.of(i, 0))
                .toList();
        int countFlights = 100;
        List<Flight> flights = generateFlights(countFlights, employees, airports, dates, times);

        // Flight assignments
        List<FlightAssignment> flightAssignments = generateFlightAssignments(flights);

        // Update problem facts
        schedule.setAirports(airports);
        schedule.setEmployees(employees);
        schedule.setFlights(flights);
        schedule.setFlightAssignments(flightAssignments);

        return schedule;
    }

    private List<Employee> generateEmployees(int size, List<Airport> airports, List<LocalDate> dates) {
        Supplier<String> nameSupplier = () -> {
            Function<String[], String> randomStringSelector = strings -> strings[random.nextInt(strings.length)];
            String firstName = randomStringSelector.apply(FIRST_NAMES);
            String lastName = randomStringSelector.apply(LAST_NAMES);
            return firstName + " " + lastName;
        };

        List<Employee> employees = IntStream.range(0, size)
                .mapToObj(i -> new Employee(String.valueOf(i), nameSupplier.get()))
                .toList();

        // Skills - 60% - Flight attendant; 40% - Pilot
        applyRandomValue((int) (0.6 * size), employees, e -> e.getSkills() == null, e -> e.setSkills(List.of(ATTENDANT_SKILL)));
        applyRandomValue((int) (0.4 * size), employees, e -> e.getSkills() == null, e -> e.setSkills(List.of(PILOT_SKILL)));
        employees.stream()
                .filter(e -> e.getSkills() == null)
                .forEach(e -> e.setSkills(List.of(ATTENDANT_SKILL)));

        // two home airports
        int firstHomeAirport = random.nextInt(airports.size());
        applyRandomValue((int) (0.5 * size), employees, e -> e.getHomeAirport() == null,
                e -> e.setHomeAirport(airports.get(firstHomeAirport)));
        int secondHomeAirport = random.nextInt(airports.size());
        applyRandomValue((int) (0.5 * size), employees, e -> e.getHomeAirport() == null,
                e -> e.setHomeAirport(airports.get(secondHomeAirport)));
        employees.stream()
                .filter(e -> e.getHomeAirport() == null)
                .forEach(e -> e.setHomeAirport(airports.get(firstHomeAirport)));

        // Unavailable dates - 28% one date; 4% two dates
        applyRandomValue((int) (0.28 * size), employees, e -> e.getUnavailableDays() == null,
                e -> e.setUnavailableDays(List.of(dates.get(random.nextInt(dates.size())))));
        applyRandomValue((int) (0.04 * size), employees, e -> e.getUnavailableDays() == null,
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

    private List<Flight> generateFlights(int size, List<Employee> employees, List<Airport> airports, List<LocalDate> dates,
            List<LocalTime> timeGroups) {
        List<Flight> flights = IntStream.range(0, size)
                .mapToObj(i -> new Flight(String.valueOf(i)))
                .toList();

        // 30% of departures from home airports; 40% for the remaining airports
        List<Airport> homeAirports = employees.stream()
                .map(Employee::getHomeAirport)
                .distinct()
                .toList();
        homeAirports.forEach(airport -> applyRandomValue((int) (0.3 * size), flights,
                flight -> flight.getDepartureAirport() == null,
                flight -> flight.setDepartureAirport(airport)));
        List<Airport> remainingAirports = airports.stream()
                .filter(airport -> employees.stream().noneMatch(e -> e.getHomeAirport().equals(airport)))
                .toList();
        int countAirports = (int) ((0.4 / remainingAirports.size()) * size);
        remainingAirports.forEach(airport -> applyRandomValue(countAirports, flights,
                flight -> flight.getDepartureAirport() == null,
                flight -> flight.setDepartureAirport(airport)));
        // Ensure there are no empty departure airports
        flights.stream()
                .filter(flight -> flight.getDepartureAirport() == null)
                .forEach(flight -> flight.setDepartureAirport(homeAirports.get(0)));

        // Flight number
        flights.forEach(
                flight -> flight.setFlightNumber("%s%s".formatted(flight.getDepartureAirport().getCode(), flight.getId())));

        // 30% of arrivals to home airports; 40% for the remaining airports
        homeAirports.forEach(airport -> applyRandomValue((int) (0.3 * size), flights,
                flight -> flight.getArrivalAirport() == null && !flight.getDepartureAirport().equals(airport),
                flight -> flight.setArrivalAirport(airport)));

        remainingAirports.forEach(airport -> applyRandomValue(countAirports, flights,
                flight -> flight.getArrivalAirport() == null && !flight.getDepartureAirport().equals(airport),
                flight -> flight.setArrivalAirport(airport)));
        // Ensure there are no empty arrival airports
        flights.stream()
                .filter(flight -> flight.getArrivalAirport() == null)
                .forEach(flight -> {
                    while (flight.getArrivalAirport() == null) {
                        Airport arrivalAirport = airports.get(random.nextInt(airports.size()));
                        if (!flight.getDepartureAirport().equals(arrivalAirport)) {
                            flight.setDepartureAirport(arrivalAirport);
                        }
                    }
                });

        // Flight duration - 1h 16%; 2h 32%; 3h 48%; 4h 4%
        List<Pair<Float, Integer>> timeGroupCount = List.of(
                new Pair<>(0.16f, 1),
                new Pair<>(0.48f, 2),
                new Pair<>(0.96f, 3),
                new Pair<>(1f, 4));
        int countDates = size / dates.size();
        BiConsumer<Flight, LocalDate> flightConsumer = (flight, date) -> {
            double nextCountHours = random.nextDouble();
            int countHours = timeGroupCount.stream()
                    .filter(p -> p.key() <= nextCountHours)
                    .mapToInt(Pair::value)
                    .findFirst()
                    .getAsInt();
            LocalTime startTime = timeGroups.get(random.nextInt(timeGroupCount.size() - countHours));
            LocalDateTime departureDateTime = LocalDateTime.of(date, startTime);
            LocalDateTime arrivalDateTime = LocalDateTime.of(date, startTime.plusHours(countHours));
            flight.setDepartureUTCDateTime(departureDateTime);
            flight.setArrivalUTCDateTime(arrivalDateTime);
        };
        dates.forEach(startDate -> applyRandomValue(countDates, flights, startDate,
                flight -> flight.getDepartureUTCDateTime() == null, flightConsumer));
        return flights;
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
