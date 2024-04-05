package org.acme.meetingschedule.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class TimeGrain implements Comparable<TimeGrain> {

    private static final Comparator<TimeGrain> COMPARATOR = Comparator.comparing(TimeGrain::getDayOfYear)
            .thenComparingInt(TimeGrain::getStartingMinuteOfDay);

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("E", Locale.ENGLISH);

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
    public String getTimeString() {
        int hourOfDay = startingMinuteOfDay / 60;
        int minuteOfHour = startingMinuteOfDay % 60;
        return (hourOfDay < 10 ? "0" : "") + hourOfDay
                + ":" + (minuteOfHour < 10 ? "0" : "") + minuteOfHour;
    }

    @JsonIgnore
    public String getDateTimeString() {
        return DAY_FORMATTER.format(getDate()) + " " + getTimeString();
    }

    @Override
    public String toString() {
        return grainIndex + "(" + getDateTimeString() + ")";
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

    @Override
    public int compareTo(TimeGrain other) {
        return COMPARATOR.compare(this, other);
    }
}
