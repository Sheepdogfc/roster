package org.acme.projectjobschedule.domain.resource;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = GlobalResource.class, name = "global"),
        @JsonSubTypes.Type(value = LocalResource.class, name = "local"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonIdentityInfo(scope = Resource.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public abstract class Resource {

    private String id;
    private int capacity;

    protected Resource() {
    }

    public Resource(String id) {
        this.id = id;
    }

    protected Resource(String id, int capacity) {
        this(id);
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public abstract boolean isRenewable();

}
