package org.acme.sportsleagueschedule.solver;

import java.util.Map;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.acme.sportsleagueschedule.domain.Day;
import org.acme.sportsleagueschedule.domain.LeagueSchedule;
import org.acme.sportsleagueschedule.domain.Match;
import org.acme.sportsleagueschedule.domain.Team;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SportsLeagueSchedulingConstraintProviderTest {

    @Inject
    ConstraintVerifier<SportsLeagueSchedulingConstraintProvider, LeagueSchedule> constraintVerifier;

    @Test
    void matchesSameDay() {
        Team homeTeam = new Team("1");
        Team rivalTeam = new Team("2");
        Match firstMatch = new Match("1", homeTeam, rivalTeam);
        firstMatch.setDay(new Day(0));
        Match secondMatch = new Match("2", homeTeam, rivalTeam);
        secondMatch.setDay(new Day(0));
        Match thirdMatch = new Match("3", homeTeam, rivalTeam);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::matchesOnSameDay)
                .given(firstMatch, secondMatch, thirdMatch)
                .penalizesBy(1); // home team plays two matches
    }

    @Test
    void fourConsecutiveHomeMatches() {
        Team homeTeam = new Team("1");
        Team rivalTeam = new Team("2");
        Match firstMatch = new Match("1", homeTeam, rivalTeam);
        firstMatch.setDay(new Day(0));
        Match secondMatch = new Match("2", homeTeam, rivalTeam);
        secondMatch.setDay(new Day(1));
        Match thirdMatch = new Match("3", homeTeam, rivalTeam);
        thirdMatch.setDay(new Day(2));
        Match fourthMatch = new Match("4", homeTeam, rivalTeam);
        fourthMatch.setDay(new Day(3));
        Match fifthMatch = new Match("5", new Team("3"), homeTeam);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::fourConsecutiveHomeMatches)
                .given(firstMatch, secondMatch, thirdMatch, fourthMatch, fifthMatch)
                .penalizesBy(1); // four consecutive matches for homeTeam
    }

    @Test
    void fourConsecutiveAwayMatches() {
        Team homeTeam = new Team("1");
        Team rivalTeam = new Team("2");
        Match firstMatch = new Match("1", homeTeam, rivalTeam);
        firstMatch.setDay(new Day(0));
        Match secondMatch = new Match("2", homeTeam, rivalTeam);
        secondMatch.setDay(new Day(1));
        Match thirdMatch = new Match("3", homeTeam, rivalTeam);
        thirdMatch.setDay(new Day(2));
        Match fourthMatch = new Match("4", homeTeam, rivalTeam);
        fourthMatch.setDay(new Day(3));
        Match fifthMatch = new Match("5", new Team("3"), homeTeam);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::fourConsecutiveAwayMatches)
                .given(firstMatch, secondMatch, thirdMatch, fourthMatch, fifthMatch)
                .penalizesBy(1); // four consecutive away matches for homeTeam
    }

    @Test
    void repeatMatchOnTheNextDay() {
        Team homeTeam = new Team("1");
        Team rivalTeam = new Team("2");
        Match firstMatch = new Match("1", homeTeam, rivalTeam);
        firstMatch.setDay(new Day(0));
        Match secondMatch = new Match("2", rivalTeam, homeTeam);
        secondMatch.setDay(new Day(1));
        Match thirdMatch = new Match("3", homeTeam, rivalTeam);
        thirdMatch.setDay(new Day(4));
        Match fourthMatch = new Match("4", rivalTeam, homeTeam);
        fourthMatch.setDay(new Day(6));

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::repeatMatchOnTheNextDay)
                .given(firstMatch, secondMatch, thirdMatch, fourthMatch)
                .penalizesBy(1); // one match repeating on the next day
    }

    @Test
    void startToAwayHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        secondTeam.setDistanceToTeam(Map.of(homeTeam, 5));
        Team thirdTeam = new Team("3");
        Match firstMatch = new Match("1", homeTeam, secondTeam);
        Day firstDay = new Day(0);
        firstMatch.setDay(firstDay);
        Match secondMatch = new Match("2", homeTeam, thirdTeam);
        Day secondDay = new Day(1);
        secondMatch.setDay(secondDay);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::startToAwayHop)
                .given(firstMatch, secondMatch, firstDay, secondDay)
                .penalizesBy(5); // match with the second team
    }

    @Test
    void homeToAwayHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        Team thirdTeam = new Team("3");
        homeTeam.setDistanceToTeam(Map.of(thirdTeam, 7));
        Match firstMatch = new Match("1", homeTeam, secondTeam);
        Day firstDay = new Day(0);
        firstMatch.setDay(firstDay);
        Match secondMatch = new Match("2", thirdTeam, homeTeam);
        Day secondDay = new Day(1);
        secondMatch.setDay(secondDay);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::homeToAwayHop)
                .given(firstMatch, secondMatch, firstDay, secondDay)
                .penalizesBy(7); // match with the home team
    }

    @Test
    void awayToAwayHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        Team thirdTeam = new Team("3");
        secondTeam.setDistanceToTeam(Map.of(thirdTeam, 2));
        Match firstMatch = new Match("1", secondTeam, homeTeam);
        Day firstDay = new Day(0);
        firstMatch.setDay(firstDay);
        Match secondMatch = new Match("2", thirdTeam, homeTeam);
        Day secondDay = new Day(1);
        secondMatch.setDay(secondDay);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::awayToAwayHop)
                .given(firstMatch, secondMatch, firstDay, secondDay)
                .penalizesBy(2); // match with the home team
    }

    @Test
    void awayToHomeHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        Team thirdTeam = new Team("3");
        secondTeam.setDistanceToTeam(Map.of(homeTeam, 20));
        Match firstMatch = new Match("1", secondTeam, homeTeam);
        Day firstDay = new Day(0);
        firstMatch.setDay(firstDay);
        Match secondMatch = new Match("2", homeTeam, thirdTeam);
        Day secondDay = new Day(1);
        secondMatch.setDay(secondDay);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::awayToHomeHop)
                .given(firstMatch, secondMatch, firstDay, secondDay)
                .penalizesBy(20); // match with the home team
    }

    @Test
    void awayToEndHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        Team thirdTeam = new Team("3");
        thirdTeam.setDistanceToTeam(Map.of(homeTeam, 15));
        Match firstMatch = new Match("1", homeTeam, secondTeam);
        Day firstDay = new Day(0);
        firstMatch.setDay(firstDay);
        Match secondMatch = new Match("2", thirdTeam, homeTeam);
        Day secondDay = new Day(1);
        secondMatch.setDay(secondDay);

        constraintVerifier.verifyThat(SportsLeagueSchedulingConstraintProvider::awayToEndHop)
                .given(firstMatch, secondMatch, firstDay, secondDay)
                .penalizesBy(15); // match with the home team
    }
}
