package com.suguna.weighment_api_service.dto.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class BasePageRequest {

    @Min(0)
    @Builder.Default
    private int page = 0;

    @Min(1)
    @Max(200)
    @Builder.Default
    private int size = 20;

    private String sortBy;
    @Builder.Default
    private Sort.Direction direction = Sort.Direction.ASC;

    public Pageable toPageable() {
        if (sortBy == null || sortBy.isBlank()) {
            return PageRequest.of(page, size);
        }
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
