package com.foodstagram.controller.form;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryModifyForm {

    private Long id;

    @NotBlank(message = "카테고리명을 작성해주세요.")
    private String name;

    private Boolean isDel;
}
