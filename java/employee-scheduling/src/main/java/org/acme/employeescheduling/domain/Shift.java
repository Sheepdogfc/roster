package org.acme.employeescheduling.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Shift {
    @PlanningId
    private String id;

    private LocalDateTime start;
    private LocalDateTime end;

    private String location;
    private String requiredSkill;

    @PlanningVariable
    private Employee employee;

    public Shift() {
    }

    public Shift(LocalDateTime start, LocalDateTime end, String location, String requiredSkill) {
        this(start, end, location, requiredSkill, null);
    }

    public Shift(LocalDateTime start, LocalDateTime end, String location, String requiredSkill, Employee employee) {
        this(null, start, end, location, requiredSkill, employee);
    }

    public Shift(String id, LocalDateTime start, LocalDateTime end, String location, String requiredSkill, Employee employee) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.location = location;
        this.requiredSkill = requiredSkill;
        this.employee = employee;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getRequiredSkill() {
        return requiredSkill;
    }

    public void setRequiredSkill(String requiredSkill) {
        this.requiredSkill = requiredSkill;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    @JsonIgnore
    public boolean isOverlappingWith(Collection<LocalDate> dates) {
        return !getOverlappingDatesForShift(dates).isEmpty();
    }

    @JsonIgnore
    public int getOverlappingDurationInMinutes(Collection<LocalDate> dates) {
        List<LocalDate> overlappingDates = getOverlappingDatesForShift(dates);
        long overlappingDurationInMinutes = 0;
        for (LocalDate date : overlappingDates) {
            overlappingDurationInMinutes += getOverlappingDurationForShift(date);
        }
        return (int) overlappingDurationInMinutes;
    }

    private List<LocalDate> getOverlappingDatesForShift(Collection<LocalDate> dates) {
        return dates.stream()
                .filter(d -> getOverlappingDurationForShift(d) > 0)
                .toList();
    }

    private long getOverlappingDurationForShift(LocalDate date) {
        LocalDateTime startDateTime = LocalDateTime.of(date, LocalTime.MIN);
        LocalDateTime endDateTime = LocalDateTime.of(date, LocalTime.MAX);
        return getOverlappingDurationInMinutes(startDateTime, endDateTime, getStart(), getEnd());
    }

    private long getOverlappingDurationInMinutes(LocalDateTime firstStartDateTime, LocalDateTime firstEndDateTime,
            LocalDateTime secondStartDateTime, LocalDateTime secondEndDateTime) {
        LocalDateTime maxStartTime = firstStartDateTime.isAfter(secondStartDateTime) ? firstStartDateTime : secondStartDateTime;
        LocalDateTime minEndTime = firstEndDateTime.isBefore(secondEndDateTime) ? firstEndDateTime : secondEndDateTime;
        long minutes = maxStartTime.until(minEndTime, ChronoUnit.MINUTES);
        return minutes > 0 ? minutes : 0;
    }

    @Override
    public String toString() {
        return location + " " + start + "-" + end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Shift shift)) {
            return false;
        }
        return Objects.equals(getId(), shift.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
