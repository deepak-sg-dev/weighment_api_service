package com.suguna.weighment_api_service.admin.dtos.requests;



import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDTO {

    @Schema(example = "01040025")
    private String userName;

    @Schema(example = "123456")
    private String password;

    /*@Schema(example = "USER_ID")
    private Long userId;*/

    @Schema(example = "Sathya Narayanan")
    private String name;

    @Schema(example = "9944033789")
    private Long mobile;

    @Schema(example = "user@sugunafoods.com")
    private String email;

    /*@Schema(example = "1234")
    private Long orgId;*/

    @Schema(example = "USER_TYPE")
    private String userType;

    @Schema(example = "POS_APP")
    private String application;

    @Schema(example = "1")
    private Long roleId;

    @Schema(example = "28558")
    private Long branchId;

    @Schema(example = "ACTIVE") // ACTIVE //INACTIVE
    private String status;
}
