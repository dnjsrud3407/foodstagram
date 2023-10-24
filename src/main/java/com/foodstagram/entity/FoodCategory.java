package com.foodstagram.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodCategory extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_category_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private Food food;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private Boolean isDel;

    private void setCategory(Category category) {
        this.category = category;
    }

    private void setDel(Boolean del) {
        isDel = del;
    }

    //== 연관관계 메서드 ==//
    public void changeFood(Food food) {
        this.food = food;
        food.getFoodCategories().add(this);
    }

    //== 생성 메서드 ==//
    public static FoodCategory createFoodCategory(Category category) {
        FoodCategory foodCategory = new FoodCategory();
        foodCategory.setCategory(category);
        foodCategory.setCreatedDate(LocalDateTime.now());
        foodCategory.setDel(false);

        return foodCategory;
    }

    //== 변경 메서드 ==//
    public void changeIsDel(Boolean value) {
        this.setDel(value);
    }

}
