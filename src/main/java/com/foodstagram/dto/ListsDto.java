package com.foodstagram.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
public class ListsDto {
    private Long id;
    private String name;
    private Long totalCnt;

    @QueryProjection
    public ListsDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @QueryProjection
    public ListsDto(Long id, String name, Long totalCnt) {
        this.id = id;
        this.name = name;
        this.totalCnt = totalCnt;
    }
}
