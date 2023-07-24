package com.foodstagram.dto;

import com.foodstagram.controller.form.CategoryCreateForm;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateDto {

    private String name;
    private Boolean isDel;

    public CategoryCreateDto(CategoryCreateForm form) {
        name = form.getName();
        isDel = form.getIsDel();
    }
}
