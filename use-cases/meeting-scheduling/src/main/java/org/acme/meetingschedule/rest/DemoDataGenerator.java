package org.acme.meetingschedule.rest;

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
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.impl.util.MutableInt;
import ai.timefold.solver.core.impl.util.Pair;

import org.acme.meetingschedule.domain.Meeting;
import org.acme.meetingschedule.domain.MeetingAssignment;
import org.acme.meetingschedule.domain.MeetingConstraintConfiguration;
import org.acme.meetingschedule.domain.MeetingSchedule;
import org.acme.meetingschedule.domain.Person;
import org.acme.meetingschedule.domain.Room;
import org.acme.meetingschedule.domain.TimeGrain;

@ApplicationScoped
public class DemoDataGenerator {

    private static final String[] FIRST_NAMES = { "Amy", "Beth", "Chad", "Dan", "Elsa", "Flo", "Gus", "Hugo", "Ivy", "Jay",
            "Jeri", "Hope", "Avis", "Lino", "Lyle", "Nick", "Dino", "Otha", "Gwen", "Jose", "Dena", "Jana", "Dave",
            "Russ", "Josh", "Dana", "Katy" };
    private static final String[] LAST_NAMES =
            { "Cole", "Fox", "Green", "Jones", "King", "Li", "Poe", "Rye", "Smith", "Watt", "Howe", "Lowe", "Wise", "Clay",
                    "Carr", "Hood", "Long", "Horn", "Haas", "Meza" };
    private final Random random = new Random(0);

    public MeetingSchedule generateDemoData() {
        MeetingSchedule schedule = new MeetingSchedule();
        schedule.setConstraintConfiguration(new MeetingConstraintConfiguration());
        // People
        int countPeople = 40;
        List<Person> people = generatePeople(countPeople);
        // Time grain
        List<TimeGrain> timeGrains = generateTimeGrain();
        // Rooms
        List<Room> rooms = List.of(
                new Room("R1", "Room 1", 30),
                new Room("R2", "Room 2", 20),
                new Room("R3", "Room 3", 16),
                new Room("R4", "Room 4", 14),
                new Room("R5", "Room 5", 12));
        // Meetings
        List<Meeting> meetings = generateMeetings(people);
        // Meeting assignments
        List<MeetingAssignment> meetingAssignments = generateMeetingAssignments(meetings);
        // Update schedule
        schedule.setRooms(rooms);
        schedule.setPeople(people);
        schedule.setTimeGrains(timeGrains);
        schedule.setMeetings(meetings);
        schedule.setMeetingAssignments(meetingAssignments);
        schedule.setAttendances(Stream.concat(
                schedule.getMeetings().stream().flatMap(m -> m.getRequiredAttendances().stream()),
                schedule.getMeetings().stream().flatMap(m -> m.getPreferredAttendances().stream()))
                .toList());
        return schedule;
    }

    private List<Person> generatePeople(int countPeople) {
        Supplier<String> nameSupplier = () -> {
            Function<String[], String> randomStringSelector = strings -> strings[random.nextInt(strings.length)];
            String firstName = randomStringSelector.apply(FIRST_NAMES);
            String lastName = randomStringSelector.apply(LAST_NAMES);
            return firstName + " " + lastName;
        };

        return IntStream.range(0, countPeople)
                .mapToObj(i -> new Person(String.valueOf(i), nameSupplier.get()))
                .toList();
    }

    private List<TimeGrain> generateTimeGrain() {
        List<TimeGrain> timeGrains = new ArrayList<>();
        LocalDate currentDate = LocalDate.now().plusDays(1);
        MutableInt count = new MutableInt();
        while (currentDate.isBefore(LocalDate.now().plusDays(6))) {
            LocalTime currentTime = LocalTime.of(8, 0);
            timeGrains.add(new TimeGrain(String.valueOf(count.increment()), count.intValue(),
                    LocalDateTime.of(currentDate, currentTime).getDayOfYear(),
                    currentTime.getHour() * 60 + currentTime.getMinute()));
            while (currentTime.isBefore(LocalTime.of(17, 45))) {
                currentTime = currentTime.plusMinutes(15);
                timeGrains.add(new TimeGrain(String.valueOf(count.increment()), count.intValue(),
                        LocalDateTime.of(currentDate, currentTime).getDayOfYear(),
                        currentTime.getHour() * 60 + currentTime.getMinute()));
            }
            currentDate = currentDate.plusDays(1);
        }
        return timeGrains;
    }

    private List<Meeting> generateMeetings(List<Person> people) {
        MutableInt count = new MutableInt();
        List<Meeting> meetings = List.of(
                new Meeting(String.valueOf(count.increment()), "Strategize B2B"),
                new Meeting(String.valueOf(count.increment()), "Fast track e-business"),
                new Meeting(String.valueOf(count.increment()), "Cross sell virtualization"),
                new Meeting(String.valueOf(count.increment()), "Profitize multitasking"),
                new Meeting(String.valueOf(count.increment()), "Transform one stop shop"),
                new Meeting(String.valueOf(count.increment()), "Engage braindumps"),
                new Meeting(String.valueOf(count.increment()), "Downsize data mining"),
                new Meeting(String.valueOf(count.increment()), "Ramp up policies"),
                new Meeting(String.valueOf(count.increment()), "On board synergies"),
                new Meeting(String.valueOf(count.increment()), "Reinvigorate user experience"),
                new Meeting(String.valueOf(count.increment()), "Strategize e-business"),
                new Meeting(String.valueOf(count.increment()), "Fast track virtualization"),
                new Meeting(String.valueOf(count.increment()), "Cross sell multitasking"),
                new Meeting(String.valueOf(count.increment()), "Profitize one stop shop"),
                new Meeting(String.valueOf(count.increment()), "Transform braindumps"),
                new Meeting(String.valueOf(count.increment()), "Engage data mining"),
                new Meeting(String.valueOf(count.increment()), "Downsize policies"),
                new Meeting(String.valueOf(count.increment()), "Ramp up synergies"),
                new Meeting(String.valueOf(count.increment()), "On board user experience"),
                new Meeting(String.valueOf(count.increment()), "Reinvigorate B2B"),
                new Meeting(String.valueOf(count.increment()), "Strategize virtualization"),
                new Meeting(String.valueOf(count.increment()), "Fast track multitasking"),
                new Meeting(String.valueOf(count.increment()), "Cross sell one stop shop"),
                new Meeting(String.valueOf(count.increment()), "Profitize braindumps"),
                new Meeting(String.valueOf(count.increment()), "Transform data mining"),
                new Meeting(String.valueOf(count.increment()), "Engage policies"),
                new Meeting(String.valueOf(count.increment()), "Downsize synergies"),
                new Meeting(String.valueOf(count.increment()), "Ramp up user experience"),
                new Meeting(String.valueOf(count.increment()), "On board B2B"),
                new Meeting(String.valueOf(count.increment()), "Reinvigorate e-business"),
                new Meeting(String.valueOf(count.increment()), "Strategize multitasking"),
                new Meeting(String.valueOf(count.increment()), "Fast track one stop shop"),
                new Meeting(String.valueOf(count.increment()), "Cross sell braindumps"),
                new Meeting(String.valueOf(count.increment()), "Profitize data mining"),
                new Meeting(String.valueOf(count.increment()), "Transform policies"),
                new Meeting(String.valueOf(count.increment()), "Engage synergies"),
                new Meeting(String.valueOf(count.increment()), "Downsize user experience"),
                new Meeting(String.valueOf(count.increment()), "Ramp up B2B"),
                new Meeting(String.valueOf(count.increment()), "On board e-business"),
                new Meeting(String.valueOf(count.increment()), "Reinvigorate multitasking"));
        // Duration
        List<Pair<Float, Integer>> durationGrainsCount = List.of(
                new Pair<>(0.18f, 1), // 18% with one grain
                new Pair<>(0.04f, 2), // 4% with two grains, etc
                new Pair<>(0.20f, 3),
                new Pair<>(0.1f, 4),
                new Pair<>(0.14f, 6),
                new Pair<>(0.16f, 8),
                new Pair<>(0.18f, 16));
        durationGrainsCount.forEach(p -> applyRandomValue((int) (p.key() * meetings.size()), meetings,
                m -> m.getDurationInGrains() == 0, m -> m.setDurationInGrains(p.value())));
        // Ensure there are no empty duration
        meetings.stream()
                .filter(m -> m.getDurationInGrains() == 0)
                .forEach(m -> m.setDurationInGrains(1));
        // Attendants
        MutableInt attendantCount = new MutableInt();
        // Required
        BiConsumer<Meeting, Integer> requiredAttendantConsumer = (meeting, size) -> {
            do {
                int nextPerson = random.nextInt(people.size());
                meeting.addAttendant(String.valueOf(attendantCount.increment()), people.get(nextPerson), true);
            } while (meeting.getRequiredAttendances().size() < size);
        };
        List<Pair<Float, Integer>> requiredAttendantsCount = List.of(
                new Pair<>(0.36f, 2), // 36% with two attendants
                new Pair<>(0.08f, 3), // 8% with three attendants, etc
                new Pair<>(0.02f, 4),
                new Pair<>(0.08f, 5),
                new Pair<>(0.08f, 6),
                new Pair<>(0.03f, 7),
                new Pair<>(0.02f, 8),
                new Pair<>(0.02f, 11),
                new Pair<>(0.03f, 12),
                new Pair<>(0.02f, 13),
                new Pair<>(0.03f, 14),
                new Pair<>(0.02f, 15),
                new Pair<>(0.02f, 17),
                new Pair<>(0.02f, 19));
        requiredAttendantsCount.forEach(p -> applyRandomValue((int) (p.key() * meetings.size()), meetings, p.value(),
                m -> m.getRequiredAttendances().isEmpty(), requiredAttendantConsumer));
        // Ensure there are no empty required attendants
        meetings.stream()
                .filter(m -> m.getRequiredAttendances() == null)
                .forEach(m -> requiredAttendantConsumer.accept(m, 2));
        // Preferred
        BiConsumer<Meeting, Integer> preferredAttendantConsumer = (meeting, size) -> {
            do {
                int nextPerson = random.nextInt(people.size());
                if (meeting.getRequiredAttendances().stream()
                        .noneMatch(requiredAttendance -> requiredAttendance.getPerson().equals(people.get(nextPerson)))) {
                    meeting.addAttendant(String.valueOf(attendantCount.increment()), people.get(nextPerson), false);
                }
            } while (meeting.getPreferredAttendances().size() < size);
        };
        List<Pair<Float, Integer>> preferredAttendantsCount = List.of(
                new Pair<>(0.06f, 1), // 6% with one attendant
                new Pair<>(0.2f, 2), // 20% with two attendants, etc
                new Pair<>(0.18f, 3),
                new Pair<>(0.06f, 4),
                new Pair<>(0.04f, 5),
                new Pair<>(0.02f, 6),
                new Pair<>(0.02f, 7),
                new Pair<>(0.02f, 8),
                new Pair<>(0.06f, 9),
                new Pair<>(0.02f, 10),
                new Pair<>(0.04f, 11),
                new Pair<>(0.02f, 14),
                new Pair<>(0.02f, 15),
                new Pair<>(0.02f, 19),
                new Pair<>(0.02f, 24));
        preferredAttendantsCount.forEach(p -> applyRandomValue((int) (p.key() * meetings.size()), meetings, p.value(),
                m -> m.getPreferredAttendances().isEmpty(), preferredAttendantConsumer));
        return meetings;
    }

    private List<MeetingAssignment> generateMeetingAssignments(List<Meeting> meetings) {
        return IntStream.range(0, meetings.size())
                .mapToObj(i -> new MeetingAssignment(String.valueOf(i), meetings.get(i)))
                .toList();
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
