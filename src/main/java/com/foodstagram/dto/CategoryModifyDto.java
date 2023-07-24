package com.foodstagram.dto;

import com.foodstagram.controller.form.CategoryModifyForm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryModifyDto {

    private Long id;
    private String name;
    private Boolean isDel;

    public CategoryModifyDto(CategoryModifyForm form) {
        id = form.getId();
        name = form.getName();
        isDel = form.getIsDel();
    }
}
