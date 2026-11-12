package com.suguna.weighment_api_service.dto.weighment.schedule;

public class ScheduleBranchDto {

    private String id;
    private String code;
    private String name;
    /** Display label derived from {@link #name} (e.g. {@code INDAPUR} from {@code M47:INDAPUR BROILER BRANCH-MH}). */
    private String shortName;
    /** HR location address for DC print ({@code hr_locations_all} via {@code sug_organization_mv}). */
    private String address;
    private String city;
    private String postalCode;
    private String state;

    public ScheduleBranchDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
