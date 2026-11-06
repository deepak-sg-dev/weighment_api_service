package com.suguna.weighment_api_service.admin.dtos.responses;




import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> data;
    private Long totalElements;
    private Integer totalPages;
}
