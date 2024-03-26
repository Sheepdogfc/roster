package org.acme.bedallocation.solver;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.bedallocation.domain.Bed;
import org.acme.bedallocation.domain.BedSchedule;
import org.acme.bedallocation.domain.Department;
import org.acme.bedallocation.domain.DepartmentSpecialism;
import org.acme.bedallocation.domain.Gender;
import org.acme.bedallocation.domain.GenderLimitation;
import org.acme.bedallocation.domain.Room;
import org.acme.bedallocation.domain.Stay;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class BedAllocationConstraintProviderTest {

    private static final LocalDate ZERO_NIGHT = LocalDate.of(2021, 2, 1);
    private static final LocalDate FIVE_NIGHT = ZERO_NIGHT.plusDays(5);

    private static final String DEFAULT_SPECIALISM = "default";

    @Inject
    ConstraintVerifier<BedAllocationConstraintProvider, BedSchedule> constraintVerifier;

    @Test
    void femaleInMaleRoom() {
        Room room = new Room();
        room.setGenderLimitation(GenderLimitation.MALE_ONLY);

        Bed bed = new Bed();
        bed.setRoom(room);

        Stay genderAdmission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM, bed);
        genderAdmission.setPatientGender(Gender.FEMALE);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::femaleInMaleRoom)
                .given(genderAdmission)
                .penalizesBy(6);
    }

    @Test
    void maleInFemaleRoom() {
        Room room = new Room();
        room.setGenderLimitation(GenderLimitation.FEMALE_ONLY);

        Bed bed = new Bed();
        bed.setRoom(room);

        Stay genderAdmission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM, bed);
        genderAdmission.setPatientGender(Gender.MALE);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::maleInFemaleRoom)
                .given(genderAdmission)
                .penalizesBy(6);
    }

    @Test
    void sameBedInSameNight() {

        Bed bed = new Bed("1");

        Stay stay = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM, bed);

        Stay sameBedAndNightsStay = new Stay("2", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM, bed);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::sameBedInSameNight)
                .given(stay, sameBedAndNightsStay)
                .penalizesBy(6);
    }

    @Test
    void departmentMinimumAge() {
        Department department = new Department("1", "Adult department");
        department.setMinimumAge(18);

        Room room = new Room();
        room.setDepartment(department);

        Bed bed = new Bed();
        bed.setRoom(room);

        Stay admission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM, bed);
        admission.setPatientAge(5);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentMinimumAge)
                .given(admission, department)
                .penalizesBy(6);
    }

    @Test
    void departmentMaximumAge() {
        Department department = new Department("2", "Underage department");
        department.setMaximumAge(18);

        Room room = new Room();
        room.setDepartment(department);

        Bed bed = new Bed();
        bed.setRoom(room);

        Stay admission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM, bed);
        admission.setPatientAge(42);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentMaximumAge)
                .given(admission, department)
                .penalizesBy(6);
    }

    @Test
    void requiredPatientEquipment() {
        Room room = new Room();
        room.setEquipments(List.of("TELEMETRY"));

        Bed bed = new Bed();
        bed.setRoom(room);

        Stay admission = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM, bed);
        admission.setPatientRequiredEquipments(List.of("TELEVISION", "TELEMETRY"));

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::requiredPatientEquipment)
                .given(admission)
                .penalizesBy(6);
    }

    @Test
    void differentGenderInSameGenderRoomInSameNight() {

        Room room = new Room("1");
        room.setGenderLimitation(GenderLimitation.SAME_GENDER);

        //Assign female
        Bed bed1 = new Bed();
        bed1.setRoom(room);

        Stay stayFemale = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM, bed1);
        stayFemale.setPatientGender(Gender.FEMALE);

        //Assign male
        Bed bed2 = new Bed();
        bed2.setRoom(room);

        Stay stayMale = new Stay("1", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM, bed2);
        stayMale.setPatientGender(Gender.MALE);

        constraintVerifier
                .verifyThat(BedAllocationConstraintProvider::differentGenderInSameGenderRoomInSameNight)
                .given(stayFemale, stayMale)
                .penalizesBy(6);
    }

    @Test
    void assignEveryPatientToABed() {

        Stay stay = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM, null);

        constraintVerifier
                .verifyThat(BedAllocationConstraintProvider::assignEveryPatientToABed)
                .given(stay)
                .penalizesBy(6);
    }

    @Test
    void preferredMaximumRoomCapacity() {


        Room room = new Room();
        room.setCapacity(6);

        Bed assignedBedInExceedCapacity = new Bed();
        assignedBedInExceedCapacity.setRoom(room);

        Stay stay = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM,
                assignedBedInExceedCapacity);
        stay.setPatientPreferredMaximumRoomCapacity(3);

        constraintVerifier
                .verifyThat(BedAllocationConstraintProvider::preferredMaximumRoomCapacity)
                .given(stay)
                .penalizesBy(6);
    }

    @Test
    void preferredPatientEquipment() {
        Room room = new Room();
        room.setEquipments(List.of("TELEMETRY"));

        Bed bed = new Bed();
        bed.setRoom(room);

        Stay stay = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, DEFAULT_SPECIALISM, bed);
        stay.setPatientPreferredEquipments(List.of("TELEVISION", "TELEMETRY"));

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::preferredPatientEquipment)
                .given(stay)
                .penalizesBy(6);
    }

    @Test
    void departmentSpecialism() {

        Department department = new Department("0", "0");

        Room roomInDep = new Room();
        roomInDep.setDepartment(department);

        Bed bedInRoomInDep = new Bed();
        bedInRoomInDep.setRoom(roomInDep);

        //Designation with 1st spec
        String spec1 = "spec1";

        Stay staySpec1 = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, spec1, bedInRoomInDep);

        //Designation with 2nd spec
        String spec2 = "spec2";

        Stay staySpec2 = new Stay("1", ZERO_NIGHT, FIVE_NIGHT, spec2, bedInRoomInDep);

        DepartmentSpecialism departmentSpecialismWithOneSpec = new DepartmentSpecialism();
        departmentSpecialismWithOneSpec.setDepartment(department);
        departmentSpecialismWithOneSpec.setSpecialism(spec1);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentSpecialism)
                .given(staySpec1, staySpec2, departmentSpecialismWithOneSpec)
                .penalizesBy(6);
    }

    @Test
    void departmentSpecialismNotFirstPriorityConstraint() {

        Department department = new Department("0", "0");
        department.setSpecialismsToPriority(Map.of("spec1", 2));

        Room roomInDep = new Room("1");
        roomInDep.setDepartment(department);
        department.addRoom(roomInDep);

        Bed bedInDep = new Bed();
        bedInDep.setRoom(roomInDep);

        //Designation with 1st spec
        String spec1 = "spec1";
        Stay stay1 = new Stay("0", ZERO_NIGHT, FIVE_NIGHT, spec1, bedInDep);

        //Designation with 2nd spec
        String spec2 = "spec2";
        Stay stay2 = new Stay("1", ZERO_NIGHT, FIVE_NIGHT, spec2, bedInDep);

        DepartmentSpecialism departmentSpecialism = new DepartmentSpecialism();
        departmentSpecialism.setDepartment(department);
        departmentSpecialism.setSpecialism(spec1);
        departmentSpecialism.setPriority(2);

        constraintVerifier.verifyThat(BedAllocationConstraintProvider::departmentSpecialismNotFirstPriority)
                .given(stay1, stay2, departmentSpecialism)
                .penalizesBy(6);
    }

}
