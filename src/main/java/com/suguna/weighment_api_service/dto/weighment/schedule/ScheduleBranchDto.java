package com.suguna.weighment_api_service.dto.weighment.schedule;

public class ScheduleBranchDto {

    private String id;
    private String code;
    private String name;
    /** Display label derived from {@link #name} (e.g. {@code INDAPUR} from {@code M47:INDAPUR BROILER BRANCH-MH}). */
    private String shortName;

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
}
