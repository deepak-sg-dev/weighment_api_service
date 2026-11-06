package com.suguna.weighment_api_service.dto.weighment.schedule;

public class PartyContactDto {

    private String id;
    private String name;
    private String mobile;

    public PartyContactDto() {}

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

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
}
