package org.acme.bedallocation.rest;

import static java.time.temporal.TemporalAdjusters.firstInMonth;
import static org.acme.bedallocation.domain.Equipment.NITROGEN;
import static org.acme.bedallocation.domain.Equipment.OXYGEN;
import static org.acme.bedallocation.domain.Equipment.TELEMETRY;
import static org.acme.bedallocation.domain.Equipment.TELEVISION;
import static org.acme.bedallocation.domain.Gender.FEMALE;
import static org.acme.bedallocation.domain.Gender.MALE;
import static org.acme.bedallocation.domain.GenderLimitation.ANY_GENDER;
import static org.acme.bedallocation.domain.GenderLimitation.FEMALE_ONLY;
import static org.acme.bedallocation.domain.GenderLimitation.MALE_ONLY;
import static org.acme.bedallocation.domain.GenderLimitation.SAME_GENDER;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import jakarta.enterprise.context.ApplicationScoped;

import ai.timefold.solver.core.impl.util.Pair;

import org.acme.bedallocation.domain.Bed;
import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.DepartmentSpecialism;
import org.acme.bedallocation.domain.Equipment;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.Patient;
import org.acme.bedallocation.domain.Room;
import org.acme.bedallocation.domain.RoomSpecialism;
import org.acme.bedallocation.domain.Schedule;
import org.acme.bedallocation.domain.Specialism;
import org.acme.bedallocation.domain.Stay;

@ApplicationScoped
public class DemoDataGenerator {

    private final List<Equipment> EQUIPMENTS = List.of(TELEMETRY, TELEVISION, OXYGEN, NITROGEN);
    private final List<LocalDate> DATES = List.of(
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)), // First Monday of the month
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)).plusDays(1),
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)).plusDays(2),
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)).plusDays(3),
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)).plusDays(4),
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)).plusDays(5),
            LocalDate.now().with(firstInMonth(DayOfWeek.MONDAY)).plusDays(6));

    private final Random random = new Random(0);

    public Schedule generateDemoData() {
        Schedule schedule = new Schedule();
        // Specialism
        List<Specialism> specialisms = List.of(
                new Specialism("1", "Specialism1"),
                new Specialism("2", "Specialism2"),
                new Specialism("3", "Specialism3"));
        schedule.setSpecialisms(specialisms);
        // Department
        List<Department> departments = List.of(new Department("1", "Department"));
        schedule.setDepartments(departments);
        // DepartmentSpecialism
        List<DepartmentSpecialism> departmentSpecialisms = List.of(
                new DepartmentSpecialism("1", departments.get(0), specialisms.get(0), 1),
                new DepartmentSpecialism("2", departments.get(0), specialisms.get(1), 2),
                new DepartmentSpecialism("3", departments.get(0), specialisms.get(2), 2));
        schedule.setDepartmentSpecialisms(departmentSpecialisms);
        // Rooms
        schedule.setRooms(generateRooms(25, departments, specialisms));
        // Beds
        schedule.setBeds(generateBeds(schedule.getRooms()));
        // Patients
        schedule.setPatients(generatePatients(519));
        // Stays
        schedule.setStays(generateStays(schedule.getPatients(), specialisms));

        return schedule;
    }

    private List<Room> generateRooms(int size, List<Department> departments, List<Specialism> specialisms) {
        List<Room> rooms = IntStream.range(0, size)
                .mapToObj(i -> new Room(String.valueOf(i), "%s%d".formatted("Room", i), departments.get(0)))
                .toList();

        // Room gender limitation
        List<Pair<Float, GenderLimitation>> genderValues = List.of(
                new Pair<>(0.08f, SAME_GENDER),
                new Pair<>(0.24f, MALE_ONLY),
                new Pair<>(0.32f, FEMALE_ONLY),
                new Pair<>(0.36f, ANY_GENDER));
        genderValues.forEach(g -> applyRandomValue((int) (size * g.key()), rooms, r -> r.getGenderLimitation() == null,
                r -> r.setGenderLimitation(g.value())));
        rooms.stream()
                .filter(g -> g.getGenderLimitation() == null)
                .toList()
                .forEach(r -> r.setGenderLimitation(ANY_GENDER));

        // Room capacity
        List<Pair<Float, Integer>> capacityValues = List.of(
                new Pair<>(0.2f, 1),
                new Pair<>(0.32f, 2),
                new Pair<>(0.48f, 4));
        capacityValues.forEach(c -> applyRandomValue((int) (size * c.key()), rooms, r -> r.getCapacity() == 0,
                r -> r.setCapacity(c.value())));
        rooms.stream()
                .filter(r -> r.getCapacity() == 0)
                .toList()
                .forEach(r -> r.setCapacity(1));

        // Room specialism priority
        for (Room room : rooms) {
            specialisms.forEach(room::addSpecialism);
        }
        List<Pair<Float, Integer>> priorityValues = List.of(
                new Pair<>(0.72f, 1),
                new Pair<>(0.24f, 2),
                new Pair<>(0.06f, 4));
        List<RoomSpecialism> roomSpecialisms = rooms.stream()
                .flatMap(r -> r.getRoomSpecialisms().stream())
                .toList();
        capacityValues.forEach(
                p -> applyRandomValue((int) (priorityValues.size() * p.key()), roomSpecialisms, r -> r.getPriority() == 0,
                        r -> r.setPriority(p.value())));
        roomSpecialisms.stream()
                .filter(r -> r.getPriority() == 0)
                .toList()
                .forEach(r -> r.setPriority(1));

        // Room equipments
        // 11% - 1 equipment; 16% 2 equipments; 42% 3 equipments; 31% 4 equipments
        List<Double> countEquipments = List.of(0.11, 0.27, 0.69, 1d);
        Consumer<Room> equipmentConsumer = room -> {
            double count = random.nextDouble();
            int numEquipments = IntStream.range(0, countEquipments.size())
                    .filter(i -> count <= countEquipments.get(i))
                    .findFirst()
                    .getAsInt() + 1;
            List<Equipment> roomEquipments = new LinkedList<>(EQUIPMENTS);
            Collections.shuffle(roomEquipments, random);
            // Three equipments per room
            room.setEquipments(roomEquipments.subList(0, numEquipments));
        };
        // Only 76% of rooms have equipment
        applyRandomValue((int) (0.76 * size), rooms, r -> r.getEquipments().isEmpty(), equipmentConsumer);

        return rooms;
    }

    private List<Bed> generateBeds(List<Room> rooms) {
        // 20% - 1 bed; 32% 2 beds; 48% 4 beds
        List<Double> countBeds = List.of(0.2, 0.52, 1d);
        for (Room room : rooms) {
            double count = random.nextDouble();
            int numBeds = IntStream.range(0, countBeds.size())
                    .filter(i -> count <= countBeds.get(i))
                    .findFirst()
                    .getAsInt() + 1;
            IntStream.range(0, numBeds)
                    .forEach(i -> room.addBed(new Bed("%s-bed%d".formatted(room.getId(), i), room, i)));
        }
        return rooms.stream().flatMap(r -> r.getBeds().stream()).toList();
    }

    private List<Patient> generatePatients(int size) {
        List<Patient> patients = IntStream.range(0, size)
                .mapToObj(i -> new Patient(String.valueOf(i), "Patient%d".formatted(i)))
                .toList();

        // 50% MALE - 50% FEMALE
        applyRandomValue(50, patients, p -> p.getGender() == null, p -> p.setGender(MALE));
        applyRandomValue(50, patients, p -> p.getGender() == null, p -> p.setGender(FEMALE));

        // Age group
        List<Pair<Float, Integer[]>> ageValues = List.of(
                new Pair<>(0.1f, new Integer[] { 0, 10 }),
                new Pair<>(0.09f, new Integer[] { 11, 20 }),
                new Pair<>(0.06f, new Integer[] { 21, 30 }),
                new Pair<>(0.1f, new Integer[] { 31, 40 }),
                new Pair<>(0.09f, new Integer[] { 41, 50 }),
                new Pair<>(0.09f, new Integer[] { 51, 60 }),
                new Pair<>(0.08f, new Integer[] { 51, 60 }),
                new Pair<>(0.07f, new Integer[] { 61, 70 }),
                new Pair<>(0.11f, new Integer[] { 71, 80 }),
                new Pair<>(0.08f, new Integer[] { 81, 90 }),
                new Pair<>(0.09f, new Integer[] { 91, 100 }),
                new Pair<>(0.06f, new Integer[] { 101, 109 }));

        ageValues.forEach(ag -> applyRandomValue((int) (ag.key() * size), patients, a -> a.getAge() == -1,
                p -> p.setAge(random.nextInt(ag.value()[0], ag.value()[1] + 1))));
        patients.stream()
                .filter(p -> p.getAge() == -1)
                .toList()
                .forEach(p -> p.setAge(71));

        // Preferred maximum capacity
        List<Pair<Float, Integer>> capacityValues = List.of(
                new Pair<>(0.34f, 1),
                new Pair<>(0.68f, 2),
                new Pair<>(1f, 4));
        for (Patient patient : patients) {
            double count = random.nextDouble();
            IntStream.range(0, capacityValues.size())
                    .filter(i -> count <= capacityValues.get(i).key())
                    .map(i -> capacityValues.get(i).value())
                    .findFirst()
                    .ifPresent(patient::setPreferredMaximumRoomCapacity);
        }

        // Required equipments - 12% no equipments; 47% one equipment; 41% two equipments
        List<Pair<Float, Equipment>> oneEquipmentValues = List.of(
                new Pair<>(0.22f, NITROGEN),
                new Pair<>(0.47f, TELEVISION),
                new Pair<>(0.72f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        BiConsumer<Patient, List<Pair<Float, Equipment>>> oneEquipmentConsumer = (patient, values) -> {
            double count = random.nextDouble();
            IntStream.range(0, values.size())
                    .filter(i -> count <= values.get(i).key())
                    .mapToObj(i -> values.get(i).value())
                    .findFirst()
                    .ifPresent(patient::addRequiredEquipment);
        };
        applyRandomValue((int) (size * 0.47), patients, oneEquipmentValues, p -> p.getRequiredEquipments().isEmpty(),
                oneEquipmentConsumer);
        List<Pair<Float, Equipment>> twoEquipmentValues = List.of(
                new Pair<>(0.13f, NITROGEN),
                new Pair<>(0.29f, TELEVISION),
                new Pair<>(0.49f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        Consumer<Patient> twoEquipmentsConsumer = patient -> {
            while (patient.getRequiredEquipments().size() < 2) {
                oneEquipmentConsumer.accept(patient, twoEquipmentValues);
            }
        };
        applyRandomValue((int) (size * 0.41), patients, p -> p.getRequiredEquipments().isEmpty(), twoEquipmentsConsumer);

        // Preferred equipments - 29% one equipment; 53% two equipments; 16% three equipments; 2% four equipments
        List<Pair<Float, Equipment>> onePreferredEquipmentValues = List.of(
                new Pair<>(0.34f, NITROGEN),
                new Pair<>(0.63f, TELEVISION),
                new Pair<>(1f, OXYGEN));
        BiConsumer<Patient, List<Pair<Float, Equipment>>> onePreferredEquipmentConsumer = (patient, values) -> {
            double count = random.nextDouble();
            IntStream.range(0, values.size())
                    .filter(i -> count <= values.get(i).key())
                    .mapToObj(i -> values.get(i).value())
                    .findFirst()
                    .ifPresent(patient::addRequiredEquipment);
        };
        applyRandomValue((int) (size * 0.29), patients, onePreferredEquipmentValues, p -> p.getPreferredEquipments().isEmpty(),
                onePreferredEquipmentConsumer);
        List<Pair<Float, Equipment>> twoPreferredEquipmentValues = List.of(
                new Pair<>(0.32f, NITROGEN),
                new Pair<>(0.62f, TELEVISION),
                new Pair<>(0.90f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        Consumer<Patient> twoPreferredEquipmentsConsumer = patient -> {
            while (patient.getRequiredEquipments().size() < 2) {
                oneEquipmentConsumer.accept(patient, twoPreferredEquipmentValues);
            }
        };
        applyRandomValue((int) (size * 0.53), patients, p -> p.getPreferredEquipments().isEmpty(),
                twoPreferredEquipmentsConsumer);
        List<Pair<Float, Equipment>> threePreferredEquipmentValues = List.of(
                new Pair<>(0.26f, NITROGEN),
                new Pair<>(0.50f, TELEVISION),
                new Pair<>(0.77f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        Consumer<Patient> threePreferredEquipmentsConsumer = patient -> {
            while (patient.getRequiredEquipments().size() < 3) {
                oneEquipmentConsumer.accept(patient, threePreferredEquipmentValues);
            }
        };
        applyRandomValue((int) (size * 0.16), patients, p -> p.getPreferredEquipments().isEmpty(),
                threePreferredEquipmentsConsumer);
        List<Pair<Float, Equipment>> fourPreferredEquipmentValues = List.of(
                new Pair<>(0.25f, NITROGEN),
                new Pair<>(0.50f, TELEVISION),
                new Pair<>(0.75f, OXYGEN),
                new Pair<>(1f, TELEMETRY));
        Consumer<Patient> fourPreferredEquipmentsConsumer = patient -> {
            while (patient.getRequiredEquipments().size() < 4) {
                oneEquipmentConsumer.accept(patient, fourPreferredEquipmentValues);
            }
        };
        applyRandomValue((int) (size * 0.16), patients, p -> p.getPreferredEquipments().isEmpty(),
                fourPreferredEquipmentsConsumer);

        patients.stream()
                .filter(p -> p.getPreferredEquipments().isEmpty())
                .toList()
                .forEach(p -> oneEquipmentConsumer.accept(p, onePreferredEquipmentValues));

        return patients;
    }

    private List<Stay> generateStays(List<Patient> patients, List<Specialism> specialisms) {
        List<Stay> stays = IntStream.range(0, patients.size())
                .mapToObj(i -> new Stay("stay-%s".formatted(patients.get(i).getId()), patients.get(i)))
                .toList();

        // Specialism - 27% Specialism1; 36% Specialism2; 37% Specialism3
        applyRandomValue((int) (0.27 * patients.size()), stays, s -> s.getSpecialism() == null,
                s -> s.setSpecialism(specialisms.get(0)));
        applyRandomValue((int) (0.36 * patients.size()), stays, s -> s.getSpecialism() == null,
                s -> s.setSpecialism(specialisms.get(1)));
        applyRandomValue((int) (0.37 * patients.size()), stays, s -> s.getSpecialism() == null,
                s -> s.setSpecialism(specialisms.get(2)));
        stays.stream()
                .filter(s -> s.getSpecialism() == null)
                .toList()
                .forEach(s -> s.setSpecialism(specialisms.get(0)));

        // Start date - 18% Mon/Fri and 5% Sat/Sun

        return stays;
    }

    private <T> void applyRandomValue(int count, List<T> values, Predicate<T> filter, Consumer<T> consumer) {
        int size = (int) values.stream().filter(filter).count();
        for (int i = 0; i < count; i++) {
            values.stream()
                    .filter(filter)
                    .skip(random.nextInt(size)).findFirst()
                    .ifPresent(consumer::accept);
            size--;
        }
    }

    private <T, L> void applyRandomValue(int count, List<T> values, List<L> secondValues, Predicate<T> filter,
            BiConsumer<T, List<L>> consumer) {
        int size = (int) values.stream().filter(filter).count();
        for (int i = 0; i < count; i++) {
            values.stream()
                    .filter(filter)
                    .skip(random.nextInt(size)).findFirst()
                    .ifPresent(v -> consumer.accept(v, secondValues));
            size--;
        }
    }
}
