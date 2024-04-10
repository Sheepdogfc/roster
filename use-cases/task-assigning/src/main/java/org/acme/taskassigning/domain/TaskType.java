package org.acme.taskassigning.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = TaskType.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "code")
public class TaskType {

    private String code;
    private String title;
    private int baseDuration; // In minutes
    private List<String> requiredSkillList;

    public TaskType() {
    }

    public TaskType(String code, String title, int baseDuration) {
        this.code = code;
        this.title = title;
        this.baseDuration = baseDuration;
        requiredSkillList = new ArrayList<>();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getBaseDuration() {
        return baseDuration;
    }

    public void setBaseDuration(int baseDuration) {
        this.baseDuration = baseDuration;
    }

    public List<String> getRequiredSkillList() {
        return requiredSkillList;
    }

    public void setRequiredSkillList(List<String> requiredSkillList) {
        this.requiredSkillList = requiredSkillList;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    public String toString() {
        return code;
    }

}
