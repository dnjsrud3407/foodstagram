package com.foodstagram.dto;

import com.foodstagram.controller.form.FoodModifyForm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodModifyDto {

    private Long foodId;

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

    private Boolean isThumbnailChange;

    private Boolean isFoodPicturesChange;

    public FoodModifyDto(FoodModifyForm form, Boolean isThumbnailChange, Boolean isFoodPicturesChange) {
        foodId = form.getFoodId();
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
        this.isThumbnailChange = isThumbnailChange;
        this.isFoodPicturesChange = isFoodPicturesChange;
    }
}
