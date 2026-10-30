package com.suguna.weighment_api_service.dto.weighment.schedule;

public class FarmDto {

    private String id;
    private String name;
    private String location;
    private double latitude;
    private double longitude;
    private int geofenceRadiusMeters;

    public FarmDto() {}

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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getGeofenceRadiusMeters() {
        return geofenceRadiusMeters;
    }

    public void setGeofenceRadiusMeters(int geofenceRadiusMeters) {
        this.geofenceRadiusMeters = geofenceRadiusMeters;
    }
}
