package com.suguna.weighment_api_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.auth")
public class AuthSeedProperties {

    private List<SeedDevice> devices = new ArrayList<>();

    public List<SeedDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<SeedDevice> devices) {
        this.devices = devices;
    }

    public static class SeedDevice {

        private String deviceId;
        private boolean enabled = true;
        private boolean authorized = true;
        private String supervisorId;
        private String supervisorCode;
        private String supervisorName;
        private String supervisorMobile;
        private String branchId;
        private String branchName;
        private boolean supervisorActive = true;

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAuthorized() {
            return authorized;
        }

        public void setAuthorized(boolean authorized) {
            this.authorized = authorized;
        }

        public String getSupervisorId() {
            return supervisorId;
        }

        public void setSupervisorId(String supervisorId) {
            this.supervisorId = supervisorId;
        }

        public String getSupervisorCode() {
            return supervisorCode;
        }

        public void setSupervisorCode(String supervisorCode) {
            this.supervisorCode = supervisorCode;
        }

        public String getSupervisorName() {
            return supervisorName;
        }

        public void setSupervisorName(String supervisorName) {
            this.supervisorName = supervisorName;
        }

        public String getSupervisorMobile() {
            return supervisorMobile;
        }

        public void setSupervisorMobile(String supervisorMobile) {
            this.supervisorMobile = supervisorMobile;
        }

        public String getBranchId() {
            return branchId;
        }

        public void setBranchId(String branchId) {
            this.branchId = branchId;
        }

        public String getBranchName() {
            return branchName;
        }

        public void setBranchName(String branchName) {
            this.branchName = branchName;
        }

        public boolean isSupervisorActive() {
            return supervisorActive;
        }

        public void setSupervisorActive(boolean supervisorActive) {
            this.supervisorActive = supervisorActive;
        }
    }
}
