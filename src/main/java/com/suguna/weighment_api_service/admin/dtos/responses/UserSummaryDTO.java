package com.suguna.weighment_api_service.admin.dtos.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSummaryDTO {

    private long totalUsers;

    private long activeUsers;

    private long employeesAndStaff;

    private long partnersAndCustomers;
}
