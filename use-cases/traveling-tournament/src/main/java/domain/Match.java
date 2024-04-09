package domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Match {

    private String id;
    private Team homeTeam;
    private Team awayTeam;
    @PlanningVariable
    private Day day;

    public Match() {
    }

    public Match(String id) {
        this.id = id;
    }

    public Match(String id, Team homeTeam, Team awayTeam) {
        this(id);
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public void setHomeTeam(Team homeTeam) {
        this.homeTeam = homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public void setAwayTeam(Team awayTeam) {
        this.awayTeam = awayTeam;
    }

    public Day getDay() {
        return day;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    @Override
    public String toString() {
        return homeTeam + "+" + awayTeam;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************
    @JsonIgnore
    public int getDayIndex() {
        return getDay().getIndex();
    }
}
