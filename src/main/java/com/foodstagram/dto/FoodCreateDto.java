package com.foodstagram.dto;

import com.foodstagram.controller.form.FoodCreateForm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodCreateDto {

    private String storeName;

    private List<Long> categoryIds;

    private LocalDate visitDate;

    private Float score;

    private Long listId;

    private String title;

    private String content;

    private String address;

    private Double latitude;

    private Double longitude;

    public FoodCreateDto(FoodCreateForm form) {
        storeName = form.getStoreName();
        categoryIds = form.getCategoryIds();
        visitDate = form.getVisitDate();
        score = form.getScore();
        listId = form.getListId();
        title = form.getTitle();
        content = form.getContent();
        address = form.getAddress();
        latitude = form.getLatitude();
        longitude = form.getLongitude();
    }
}
