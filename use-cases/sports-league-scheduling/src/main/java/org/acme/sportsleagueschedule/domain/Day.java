package org.acme.sportsleagueschedule.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Day.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "index")
public class Day {

    private int index;

    private Day nextDay;

    public Day() {
    }

    public Day(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Day getNextDay() {
        return nextDay;
    }

    public void setNextDay(Day nextDay) {
        this.nextDay = nextDay;
    }

    @Override
    public String toString() {
        return "Day-" + index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Day day))
            return false;
        return getIndex() == day.getIndex();
    }

    @Override
    public int hashCode() {
        return 31 * index;
    }
}
