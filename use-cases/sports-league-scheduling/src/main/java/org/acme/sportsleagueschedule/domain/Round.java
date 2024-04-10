package org.acme.sportsleagueschedule.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Round.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "index")
public class Round {

    @PlanningId
    private int index;
    private boolean importantRound;

    public Round() {
    }

    public Round(int index) {
        this.index = index;
    }

    public Round(int index, boolean importantRound) {
        this(index);
        this.importantRound = importantRound;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isImportantRound() {
        return importantRound;
    }

    public void setImportantRound(boolean importantRound) {
        this.importantRound = importantRound;
    }

    @Override
    public String toString() {
        return "Day-" + index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Round round))
            return false;
        return getIndex() == round.getIndex();
    }

    @Override
    public int hashCode() {
        return 31 * index;
    }
}
