package org.acme.meetingschedule.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = TimeGrain.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class TimeGrain {

    /**
     * Time granularity is 15 minutes (which is often recommended when dealing with humans for practical purposes).
     */
    public static final int GRAIN_LENGTH_IN_MINUTES = 15;

    @PlanningId
    private String id;
    private int grainIndex;
    private Integer dayOfYear;
    private int startingMinuteOfDay;

    public TimeGrain() {
    }

    public TimeGrain(String id) {
        this.id = id;
    }

    public TimeGrain(String id, int grainIndex, Integer dayOfYear, int startingMinuteOfDay) {
        this(id);
        this.grainIndex = grainIndex;
        this.dayOfYear = dayOfYear;
        this.startingMinuteOfDay = startingMinuteOfDay;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getGrainIndex() {
        return grainIndex;
    }

    public void setGrainIndex(int grainIndex) {
        this.grainIndex = grainIndex;
    }

    public Integer getDayOfYear() {
        return dayOfYear;
    }

    public void setDayOfYear(Integer dayOfYear) {
        this.dayOfYear = dayOfYear;
    }

    public int getStartingMinuteOfDay() {
        return startingMinuteOfDay;
    }

    public void setStartingMinuteOfDay(int startingMinuteOfDay) {
        this.startingMinuteOfDay = startingMinuteOfDay;
    }

    @JsonIgnore
    public LocalDate getDate() {
        return LocalDate.now().withDayOfYear(dayOfYear);
    }

    @JsonIgnore
    public LocalTime getTime() {
        return LocalTime.of(startingMinuteOfDay / 60, startingMinuteOfDay % 60);
    }

    @JsonIgnore
    public LocalDateTime getDateTime() {
        return LocalDateTime.of(getDate(), getTime());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (other == null || getClass() != other.getClass())
            return false;

        TimeGrain timeGrain = (TimeGrain) other;

        if (startingMinuteOfDay != timeGrain.startingMinuteOfDay)
            return false;
        return Objects.equals(dayOfYear, timeGrain.dayOfYear);
    }

    @Override
    public int hashCode() {
        int result = dayOfYear != null ? dayOfYear.hashCode() : 0;
        result = 31 * result + startingMinuteOfDay;
        return result;
    }
}
