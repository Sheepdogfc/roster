package org.acme.sportsleagueschedule.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Day.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "index")
public class Day {

    @PlanningId
    private int index;

    public Day() {
    }

    public Day(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
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
