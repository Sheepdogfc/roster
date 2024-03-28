package org.acme.flighcrewscheduling.domain;

import java.util.Objects;

public class Skill {

    private String id;
    private String name;

    public Skill() {
    }

    public Skill(String id) {
        this.id = id;
    }

    public Skill(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Simple getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Skill skill))
            return false;
        return Objects.equals(getId(), skill.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
