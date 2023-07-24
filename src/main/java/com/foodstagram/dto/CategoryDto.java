package com.foodstagram.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
public class CategoryDto {

    private Long id;
    private String name;
    private Boolean isDel;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @QueryProjection
    public CategoryDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public CategoryDto(Long id, String name, Boolean isDel, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.id = id;
        this.name = name;
        this.isDel = isDel;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }
}
