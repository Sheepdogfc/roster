package org.acme.sportsleagueschedule.solver;

import static ai.timefold.solver.core.api.score.stream.Joiners.equal;
import static ai.timefold.solver.core.api.score.stream.Joiners.filtering;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;

import org.acme.sportsleagueschedule.domain.Day;
import org.acme.sportsleagueschedule.domain.Match;

public class SportsLeagueSchedulingConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                matchesOnSameDay(constraintFactory),
                fourConsecutiveHomeMatches(constraintFactory),
                fourConsecutiveAwayMatches(constraintFactory),
                repeatMatchOnTheNextDay(constraintFactory),
                startToAwayHop(constraintFactory),
                homeToAwayHop(constraintFactory),
                awayToAwayHop(constraintFactory),
                awayToHomeHop(constraintFactory),
                awayToEndHop(constraintFactory)
        };
    }

    protected Constraint matchesOnSameDay(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Match.class,
                        equal(Match::getDayIndex),
                        filtering((match1, match2) -> match1.getHomeTeam().equals(match2.getHomeTeam())
                                || match1.getHomeTeam().equals(match2.getAwayTeam())))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("matches on the same day");
    }

    protected Constraint fourConsecutiveHomeMatches(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .ifExists(Match.class, equal(Match::getHomeTeam),
                        equal(match -> match.getDayIndex() + 1, Match::getDayIndex))
                .ifExists(Match.class, equal(Match::getHomeTeam),
                        equal(match -> match.getDayIndex() + 2, Match::getDayIndex))
                .ifExists(Match.class, equal(Match::getHomeTeam),
                        equal(match -> match.getDayIndex() + 3, Match::getDayIndex))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("4 consecutive home matches");
    }

    protected Constraint fourConsecutiveAwayMatches(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .ifExists(Match.class, equal(Match::getAwayTeam),
                        equal(match -> match.getDayIndex() + 1, Match::getDayIndex))
                .ifExists(Match.class, equal(Match::getAwayTeam),
                        equal(match -> match.getDayIndex() + 2, Match::getDayIndex))
                .ifExists(Match.class, equal(Match::getAwayTeam),
                        equal(match -> match.getDayIndex() + 3, Match::getDayIndex))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("4 consecutive away matches");
    }

    protected Constraint repeatMatchOnTheNextDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .ifExists(Match.class, equal(Match::getHomeTeam, Match::getAwayTeam),
                        equal(Match::getAwayTeam, Match::getHomeTeam),
                        equal(match -> match.getDayIndex() + 1, Match::getDayIndex))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Repeat match on the next day");
    }

    protected Constraint startToAwayHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .ifNotExists(Day.class,
                        equal(match -> match.getDayIndex() - 1, Day::getIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        match -> match.getAwayTeam().getDistance(match.getHomeTeam()))
                .asConstraint("Start to away hop");
    }

    protected Constraint homeToAwayHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .join(Match.class, equal(Match::getHomeTeam, Match::getAwayTeam),
                        equal(match -> match.getDayIndex() + 1, Match::getDayIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        (match, otherMatch) -> match.getHomeTeam().getDistance(otherMatch.getHomeTeam()))
                .asConstraint("Home to away hop");
    }

    protected Constraint awayToAwayHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .join(Match.class, equal(Match::getAwayTeam, Match::getAwayTeam),
                        equal(match -> match.getDayIndex() + 1, Match::getDayIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        (match, otherMatch) -> match.getHomeTeam().getDistance(otherMatch.getHomeTeam()))
                .asConstraint("Away to away hop");
    }

    protected Constraint awayToHomeHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .join(Match.class, equal(Match::getAwayTeam, Match::getHomeTeam),
                        equal(match -> match.getDayIndex() + 1, Match::getDayIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        (match, otherMatch) -> match.getHomeTeam().getDistance(match.getAwayTeam()))
                .asConstraint("Away to home hop");
    }

    protected Constraint awayToEndHop(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Match.class)
                .ifNotExists(Day.class, equal(match -> match.getDayIndex() + 1, Day::getIndex))
                .penalize(HardSoftScore.ONE_SOFT,
                        match -> match.getHomeTeam().getDistance(match.getAwayTeam()))
                .asConstraint("Away to end hop");
    }

}
