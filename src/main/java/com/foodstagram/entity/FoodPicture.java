package com.foodstagram.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "food_picture")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodPicture extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_picture_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private Food food;

    private String originalFileName;

    private String storedFileName;

    private Boolean isThumbnail;

    private Boolean isDel;

    private void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    private void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    private void setThumbnail(Boolean thumbnail) {
        isThumbnail = thumbnail;
    }

    private void setDel(Boolean del) {
        isDel = del;
    }

    //== 연관관계 메서드 ==//
    public void changeFood(Food food) {
        this.food = food;
        food.getFoodPictures().add(this);
    }

    //== 생성 메서드 ==//
    public static FoodPicture createFoodPicture(String originalFileName, String storedFileName, Boolean isThumbnail) {
        FoodPicture foodPicture = new FoodPicture();
        foodPicture.setOriginalFileName(originalFileName);
        foodPicture.setStoredFileName(storedFileName);
        foodPicture.setThumbnail(isThumbnail);
        foodPicture.setCreatedDate(LocalDateTime.now());
        foodPicture.setDel(false);

        return foodPicture;
    }

    //== 변경 메서드 ==//
    public void changeIsDel(Boolean value) {
        this.setDel(value);
    }
}
