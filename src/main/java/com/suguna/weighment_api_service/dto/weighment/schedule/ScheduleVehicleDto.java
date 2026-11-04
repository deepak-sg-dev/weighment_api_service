package com.suguna.weighment_api_service.dto.weighment.schedule;

public class ScheduleVehicleDto {

    private String number;
    private String driverName;
    private String driverMobile;

    public ScheduleVehicleDto() {}

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverMobile() {
        return driverMobile;
    }

    public void setDriverMobile(String driverMobile) {
        this.driverMobile = driverMobile;
    }
}
