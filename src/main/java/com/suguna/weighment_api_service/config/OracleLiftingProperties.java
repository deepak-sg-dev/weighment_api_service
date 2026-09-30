package com.suguna.weighment_api_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.oracle.lifting")
public class OracleLiftingProperties {

    /** Oracle package that owns lifting schedule APIs (APPS schema). */
    private String packageName = "sug_mai_birds_lifting_pkg";

    private String schemaName = "APPS";

    /** Log package procedures from ALL_PROCEDURES on startup (dev aid). */
    private boolean introspectOnStartup = false;

    private Procedures procedures = new Procedures();

    private Parameters parameters = new Parameters();

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public boolean isIntrospectOnStartup() {
        return introspectOnStartup;
    }

    public void setIntrospectOnStartup(boolean introspectOnStartup) {
        this.introspectOnStartup = introspectOnStartup;
    }

    public Procedures getProcedures() {
        return procedures;
    }

    public void setProcedures(Procedures procedures) {
        this.procedures = procedures;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    public static class Procedures {

        /** List assigned schedules for supervisor (REF CURSOR OUT). */
        private String listSchedules = "";

        /** Single schedule header/summary (REF CURSOR OUT). */
        private String getScheduleDetails = "";

        /** Full schedule package for offline download (REF CURSOR OUT). */
        private String getSchedulePackage = "";

        private String markScheduleDownloaded = "";
        private String startSchedule = "";

        public String getListSchedules() {
            return listSchedules;
        }

        public void setListSchedules(String listSchedules) {
            this.listSchedules = listSchedules;
        }

        public String getGetScheduleDetails() {
            return getScheduleDetails;
        }

        public void setGetScheduleDetails(String getScheduleDetails) {
            this.getScheduleDetails = getScheduleDetails;
        }

        public String getGetSchedulePackage() {
            return getSchedulePackage;
        }

        public void setGetSchedulePackage(String getSchedulePackage) {
            this.getSchedulePackage = getSchedulePackage;
        }

        public String getMarkScheduleDownloaded() {
            return markScheduleDownloaded;
        }

        public void setMarkScheduleDownloaded(String markScheduleDownloaded) {
            this.markScheduleDownloaded = markScheduleDownloaded;
        }

        public String getStartSchedule() {
            return startSchedule;
        }

        public void setStartSchedule(String startSchedule) {
            this.startSchedule = startSchedule;
        }
    }

    public static class Parameters {

        private String supervisorId = "P_SUPERVISOR_ID";
        private String scheduleId = "P_SCHEDULE_ID";
        private String statusList = "P_STATUS_LIST";
        private String deviceId = "P_DEVICE_ID";
        private String appVersion = "P_APP_VERSION";
        private String downloadedAt = "P_DOWNLOADED_AT";
        private String localTransactionId = "P_LOCAL_TRANSACTION_ID";
        private String startedAt = "P_STARTED_AT";
        private String vehicleRegistration = "P_VEHICLE_REGISTRATION";
        private String latitude = "P_LATITUDE";
        private String longitude = "P_LONGITUDE";
        private String resultCursor = "P_RESULT";
        private String returnStatus = "X_RETURN_STATUS";
        private String returnMessage = "X_RETURN_MESSAGE";

        public String getSupervisorId() {
            return supervisorId;
        }

        public void setSupervisorId(String supervisorId) {
            this.supervisorId = supervisorId;
        }

        public String getScheduleId() {
            return scheduleId;
        }

        public void setScheduleId(String scheduleId) {
            this.scheduleId = scheduleId;
        }

        public String getStatusList() {
            return statusList;
        }

        public void setStatusList(String statusList) {
            this.statusList = statusList;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        public String getDownloadedAt() {
            return downloadedAt;
        }

        public void setDownloadedAt(String downloadedAt) {
            this.downloadedAt = downloadedAt;
        }

        public String getLocalTransactionId() {
            return localTransactionId;
        }

        public void setLocalTransactionId(String localTransactionId) {
            this.localTransactionId = localTransactionId;
        }

        public String getStartedAt() {
            return startedAt;
        }

        public void setStartedAt(String startedAt) {
            this.startedAt = startedAt;
        }

        public String getVehicleRegistration() {
            return vehicleRegistration;
        }

        public void setVehicleRegistration(String vehicleRegistration) {
            this.vehicleRegistration = vehicleRegistration;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public String getResultCursor() {
            return resultCursor;
        }

        public void setResultCursor(String resultCursor) {
            this.resultCursor = resultCursor;
        }

        public String getReturnStatus() {
            return returnStatus;
        }

        public void setReturnStatus(String returnStatus) {
            this.returnStatus = returnStatus;
        }

        public String getReturnMessage() {
            return returnMessage;
        }

        public void setReturnMessage(String returnMessage) {
            this.returnMessage = returnMessage;
        }
    }
}
