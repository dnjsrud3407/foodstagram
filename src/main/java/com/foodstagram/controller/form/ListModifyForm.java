package com.foodstagram.controller.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListModifyForm {

    @NotNull
    private Long listId;

    @NotBlank(message = "리스트 이름을 입력해주세요.")
    private String modifyName;
}
