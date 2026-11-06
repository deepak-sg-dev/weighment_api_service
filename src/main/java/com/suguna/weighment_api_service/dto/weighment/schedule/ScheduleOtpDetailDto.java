package com.suguna.weighment_api_service.dto.weighment.schedule;

public class ScheduleOtpDetailDto {

    private PartyOtpDetailDto farmer;
    private PartyOtpDetailDto trader;

    public ScheduleOtpDetailDto() {}

    public PartyOtpDetailDto getFarmer() {
        return farmer;
    }

    public void setFarmer(PartyOtpDetailDto farmer) {
        this.farmer = farmer;
    }

    public PartyOtpDetailDto getTrader() {
        return trader;
    }

    public void setTrader(PartyOtpDetailDto trader) {
        this.trader = trader;
    }
}
