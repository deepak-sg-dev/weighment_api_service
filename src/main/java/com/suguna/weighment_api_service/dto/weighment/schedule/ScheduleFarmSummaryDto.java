package com.suguna.weighment_api_service.dto.weighment.schedule;

public class ScheduleFarmSummaryDto {

    private String id;
    private String name;
    private String location;

    public ScheduleFarmSummaryDto() {}

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
