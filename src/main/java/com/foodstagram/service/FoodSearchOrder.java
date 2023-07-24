package com.foodstagram.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FoodSearchOrder {
    VISIT_DATE_DESC("방문 최신순"),
    VISIT_DATE_ASC("방문 오래된 순"),
    HIGH_SCORE("평점 높은 순"),
    LOW_SCORE("평점 낮은 순");

    private String label;
}
