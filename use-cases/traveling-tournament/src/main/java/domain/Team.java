package domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Team.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Team {

    private String id;
    private String name;
    private Map<Team, Integer> distanceToTeamMap;

    public Team() {
    }

    public Team(String id) {
        this.id = id;
    }

    public Team(String id, String name) {
        this(id);
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Team, Integer> getDistanceToTeamMap() {
        return distanceToTeamMap;
    }

    public void setDistanceToTeamMap(Map<Team, Integer> distanceToTeamMap) {
        this.distanceToTeamMap = distanceToTeamMap;
    }

    @JsonIgnore
    public int getDistance(Team other) {
        return distanceToTeamMap.get(other);
    }

    @Override
    public String toString() {
        return getName();
    }

}
