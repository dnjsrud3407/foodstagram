package com.foodstagram.dto;

import com.foodstagram.controller.form.ListCreateForm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListCreateDto {
    private String name;

    public ListCreateDto(ListCreateForm form) {
        this.name = form.getName();
    }
}
