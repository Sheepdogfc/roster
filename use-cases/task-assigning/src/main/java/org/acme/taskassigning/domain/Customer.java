package org.acme.taskassigning.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Customer.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Customer {

    private String id;
    private String name;

    public Customer() {
    }

    public Customer(String id) {
        this.id = id;
    }

    public Customer(String id, String name) {
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
}
