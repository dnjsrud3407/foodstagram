package com.foodstagram.dto;

import com.foodstagram.controller.form.ListModifyForm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListModifyDto {

    private Long listId;
    private String modifyName;

    public ListModifyDto(ListModifyForm form) {
        this.listId = form.getListId();
        this.modifyName = form.getModifyName();
    }
}
