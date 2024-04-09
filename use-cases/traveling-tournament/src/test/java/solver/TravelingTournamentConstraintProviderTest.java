package solver;

import java.util.Map;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import org.junit.jupiter.api.Test;

import domain.Day;
import domain.Match;
import domain.Team;
import domain.TravelingTournament;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TravelingTournamentConstraintProviderTest {

    @Inject
    ConstraintVerifier<TravelingTournamentConstraintProvider, TravelingTournament> constraintVerifier;

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

        constraintVerifier.verifyThat(TravelingTournamentConstraintProvider::fourConsecutiveHomeMatches)
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

        constraintVerifier.verifyThat(TravelingTournamentConstraintProvider::fourConsecutiveAwayMatches)
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

        constraintVerifier.verifyThat(TravelingTournamentConstraintProvider::repeatMatchOnTheNextDay)
                .given(firstMatch, secondMatch, thirdMatch, fourthMatch)
                .penalizesBy(1); // one match repeating on the next day
    }

    @Test
    void startToAwayHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        secondTeam.setDistanceToTeamMap(Map.of(homeTeam, 5));
        Team thirdTeam = new Team("3");
        Match firstMatch = new Match("1", homeTeam, secondTeam);
        Day firstDay = new Day(0);
        firstMatch.setDay(firstDay);
        Match secondMatch = new Match("2", homeTeam, thirdTeam);
        Day secondDay = new Day(1);
        secondMatch.setDay(secondDay);

        constraintVerifier.verifyThat(TravelingTournamentConstraintProvider::startToAwayHop)
                .given(firstMatch, secondMatch, firstDay, secondDay)
                .penalizesBy(5); // match with the second team
    }

    @Test
    void homeToAwayHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        Team thirdTeam = new Team("3");
        homeTeam.setDistanceToTeamMap(Map.of(thirdTeam, 7));
        Match firstMatch = new Match("1", homeTeam, secondTeam);
        Day firstDay = new Day(0);
        firstMatch.setDay(firstDay);
        Match secondMatch = new Match("2", thirdTeam, homeTeam);
        Day secondDay = new Day(1);
        secondMatch.setDay(secondDay);

        constraintVerifier.verifyThat(TravelingTournamentConstraintProvider::homeToAwayHop)
                .given(firstMatch, secondMatch, firstDay, secondDay)
                .penalizesBy(7); // match with the home team
    }

    @Test
    void awayToAwayHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        Team thirdTeam = new Team("3");
        secondTeam.setDistanceToTeamMap(Map.of(thirdTeam, 2));
        Match firstMatch = new Match("1", secondTeam, homeTeam);
        Day firstDay = new Day(0);
        firstMatch.setDay(firstDay);
        Match secondMatch = new Match("2", thirdTeam, homeTeam);
        Day secondDay = new Day(1);
        secondMatch.setDay(secondDay);

        constraintVerifier.verifyThat(TravelingTournamentConstraintProvider::awayToAwayHop)
                .given(firstMatch, secondMatch, firstDay, secondDay)
                .penalizesBy(2); // match with the home team
    }

    @Test
    void awayToHomeHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        Team thirdTeam = new Team("3");
        secondTeam.setDistanceToTeamMap(Map.of(homeTeam, 20));
        Match firstMatch = new Match("1", secondTeam, homeTeam);
        Day firstDay = new Day(0);
        firstMatch.setDay(firstDay);
        Match secondMatch = new Match("2", homeTeam, thirdTeam);
        Day secondDay = new Day(1);
        secondMatch.setDay(secondDay);

        constraintVerifier.verifyThat(TravelingTournamentConstraintProvider::awayToHomeHop)
                .given(firstMatch, secondMatch, firstDay, secondDay)
                .penalizesBy(20); // match with the home team
    }

    @Test
    void awayToEndHop() {
        Team homeTeam = new Team("1");
        Team secondTeam = new Team("2");
        Team thirdTeam = new Team("3");
        thirdTeam.setDistanceToTeamMap(Map.of(homeTeam, 15));
        Match firstMatch = new Match("1", homeTeam, secondTeam);
        Day firstDay = new Day(0);
        firstMatch.setDay(firstDay);
        Match secondMatch = new Match("2", thirdTeam, homeTeam);
        Day secondDay = new Day(1);
        secondMatch.setDay(secondDay);

        constraintVerifier.verifyThat(TravelingTournamentConstraintProvider::awayToEndHop)
                .given(firstMatch, secondMatch, firstDay, secondDay)
                .penalizesBy(15); // match with the home team
    }
}
