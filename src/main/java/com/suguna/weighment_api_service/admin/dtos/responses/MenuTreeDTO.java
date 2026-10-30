package com.suguna.weighment_api_service.admin.dtos.responses;



import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MenuTreeDTO {

    private Long id;
    private String menuName;
    private String menuPath;
    private String menuFor;
    private String menuType;


    // ✅ Permission fields
    private boolean canRead;
    private boolean canWrite;
    private boolean canUpdate;
    private boolean canDelete;


    private List<MenuTreeDTO> children = new ArrayList<>();

}
