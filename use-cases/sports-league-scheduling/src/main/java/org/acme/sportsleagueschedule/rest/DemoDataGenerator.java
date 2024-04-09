package org.acme.sportsleagueschedule.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import jakarta.enterprise.context.ApplicationScoped;

import org.acme.sportsleagueschedule.domain.Day;
import org.acme.sportsleagueschedule.domain.LeagueSchedule;
import org.acme.sportsleagueschedule.domain.Match;
import org.acme.sportsleagueschedule.domain.Team;

@ApplicationScoped
public class DemoDataGenerator {

    private final int[][] distanceInKm = new int[][] {
            { 0, 2163, 2163, 2160, 2156, 2156, 2163, 340, 1342, 512, 3038, 1526, 2054, 2054 },
            { 2163, 0, 11, 50, 813, 813, 11, 1967, 842, 1661, 1139, 1037, 202, 202 },
            { 2163, 11, 0, 50, 813, 813, 11, 1967, 842, 1661, 1139, 1037, 202, 202 },
            { 2160, 50, 50, 0, 862, 862, 50, 1957, 831, 1655, 1180, 1068, 161, 161, },
            { 2160, 813, 813, 862, 0, 1, 813, 2083, 1160, 1741, 910, 644, 600, 600 },
            { 2160, 813, 813, 862, 1, 0, 813, 2083, 1160, 1741, 910, 644, 600, 600 },
            { 2163, 11, 11, 50, 813, 813, 0, 1967, 842, 1661, 1139, 1037, 202, 202 },
            { 340, 1967, 1967, 1957, 2083, 2083, 1967, 0, 1126, 341, 2926, 1490, 1836, 1836, },
            { 1342, 842, 842, 831, 1160, 1160, 842, 1126, 0, 831, 1874, 820, 714, 714, },
            { 512, 1661, 1661, 1655, 1741, 1741, 1661, 341, 831, 0, 2589, 1151, 1545, 1545 },
            { 3038, 1139, 1139, 1180, 910, 910, 1139, 2926, 1874, 2589, 0, 1552, 1340, 1340 },
            { 1526, 1037, 1037, 1068, 644, 644, 1037, 1490, 820, 1151, 1552, 0, 1077, 1077 },
            { 2054, 202, 202, 161, 600, 600, 202, 1836, 714, 1545, 1340, 1077, 0, 14 },
            { 2054, 202, 202, 161, 600, 600, 202, 1836, 714, 1545, 1340, 1077, 14, 0 },
    };

    public LeagueSchedule generateDemoData() {
        LeagueSchedule schedule = new LeagueSchedule();
        // Days
        int countDays = 21;
        List<Day> days = generateDays(countDays);
        // Teams
        List<Team> teams = generateTeams();
        // Matches
        List<Match> matches = generateMacthes(teams);
        // Update schedule
        schedule.setDays(days);
        schedule.setTeams(teams);
        schedule.setMatches(matches);
        return schedule;
    }

    private List<Day> generateDays(int countDays) {
        return IntStream.range(0, countDays)
                .mapToObj(Day::new)
                .toList();
    }

    private List<Team> generateTeams() {
        List<Team> teams = List.of(
                new Team("1", "Cruzeiro"),
                new Team("2", "Argentinos Juniors"),
                new Team("3", "Boca Juniors"),
                new Team("4", "Estudiantes"),
                new Team("5", "Independente"),
                new Team("6", "Racing"),
                new Team("7", "River Plate"),
                new Team("8", "Flamengo"),
                new Team("9", "Gremio"),
                new Team("10", "Santos"),
                new Team("11", "Colo-Colo"),
                new Team("12", "Olimpia"),
                new Team("13", "Nacional"),
                new Team("14", "Penharol"));

        // Distances
        for (int i = 0; i < teams.size(); i++) {
            Map<Team, Integer> distances = new HashMap<>();
            for (int j = 0; j < teams.size(); j++) {
                if (i != j) {
                    distances.put(teams.get(j), distanceInKm[i][j]);
                }
            }
            teams.get(i).setDistanceToTeam(distances);
        }

        return teams;
    }

    private List<Match> generateMacthes(List<Team> teams) {
        List<Match> matches = new ArrayList<>(teams.size() * teams.size());
        for (int i = 0; i < teams.size(); i++) {
            for (int j = 0; j < teams.size(); j++) {
                if (i != j) {
                    matches.add(new Match("%s-%s".formatted(teams.get(i).getId(), teams.get(j).getId()), teams.get(i),
                            teams.get(j)));
                }
            }
        }
        return matches;
    }
}
