package org.acme.meetingschedule.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Meeting.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Meeting {

    private String id;
    private String topic;
    private List<Person> speakers;
    private String content;
    private boolean entireGroupMeeting;
    /**
     * Multiply by {@link TimeGrain#GRAIN_LENGTH_IN_MINUTES} to get duration in minutes.
     */
    private int durationInGrains;

    private List<RequiredAttendance> requiredAttendances;
    private List<PreferredAttendance> preferredAttendances;

    public Meeting() {
    }

    public Meeting(String id) {
        this.id = id;
        this.requiredAttendances = new ArrayList<>();
        this.preferredAttendances = new ArrayList<>();
    }

    public Meeting(String id, String topic) {
        this(id);
        this.topic = topic;
    }

    public Meeting(String id, String topic, int durationInGrains) {
        this(id);
        this.topic = topic;
        this.durationInGrains = durationInGrains;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<Person> getSpeakers() {
        return speakers;
    }

    public void setSpeakers(List<Person> speakers) {
        this.speakers = speakers;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEntireGroupMeeting() {
        return entireGroupMeeting;
    }

    public void setEntireGroupMeeting(boolean entireGroupMeeting) {
        this.entireGroupMeeting = entireGroupMeeting;
    }

    public int getDurationInGrains() {
        return durationInGrains;
    }

    public void setDurationInGrains(int durationInGrains) {
        this.durationInGrains = durationInGrains;
    }

    public List<RequiredAttendance> getRequiredAttendances() {
        return requiredAttendances;
    }

    public void setRequiredAttendances(List<RequiredAttendance> requiredAttendances) {
        this.requiredAttendances = requiredAttendances;
    }

    public List<PreferredAttendance> getPreferredAttendances() {
        return preferredAttendances;
    }

    public void setPreferredAttendances(List<PreferredAttendance> preferredAttendances) {
        this.preferredAttendances = preferredAttendances;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonIgnore
    public int getRequiredCapacity() {
        return requiredAttendances.size() + preferredAttendances.size();
    }

    public void addAttendant(String id, Person person, boolean isRequired) {
        if (isRequired) {
            if (requiredAttendances.stream().noneMatch(r -> r.getPerson().equals(person))) {
                requiredAttendances.add(new RequiredAttendance(id, this, person));
            }
        } else {
            if (preferredAttendances.stream().noneMatch(r -> r.getPerson().equals(person))) {
                preferredAttendances.add(new PreferredAttendance(id, this, person));
            }
        }
    }

    @Override
    public String toString() {
        return topic;
    }
}
