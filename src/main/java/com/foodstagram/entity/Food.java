package com.foodstagram.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Food extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "food_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id")
    private Lists list;

    @OneToMany(mappedBy = "food", cascade = CascadeType.PERSIST)
    private List<FoodCategory> foodCategories = new ArrayList<>();

    @OneToMany(mappedBy = "food", cascade = CascadeType.PERSIST)
    private List<FoodPicture> foodPictures = new ArrayList<>();

    private String storeName;

    private Float score;

    private String title;

    private String content;

    private LocalDate visitDate;

    private String address;

    private Double latitude;

    private Double longitude;

    private Boolean isDel;

    private void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    private void setScore(Float score) {
        this.score = score;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    private void setContent(String content) {
        this.content = content;
    }

    private void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    private void setAddress(String address) {
        this.address = address;
    }

    private void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    private void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    private void setDel(Boolean del) {
        isDel = del;
    }

    //== 연관관계 메서드 ==//
    public void changeUser(User user) {
        this.user = user;
        user.getFoods().add(this);
    }

    public void changeList(Lists list) {
        this.list = list;
        list.getFoods().add(this);
    }

    //== 생성 메서드 ==//
    public static Food createFood(User user, Lists list, List<FoodCategory> foodCategories, List<FoodPicture> foodPictures,
                                  String storeName, Float score, String title, String content, LocalDate visitDate, String address, Double latitude, Double longitude) {
        Food food = new Food();
        food.changeUser(user);

        if(list != null) {
            food.changeList(list);
        }

        for (FoodCategory foodCategory : foodCategories) {
            foodCategory.changeFood(food);
        }

        for (FoodPicture foodPicture : foodPictures) {
            foodPicture.changeFood(food);
        }

        food.setStoreName(storeName);
        food.setScore(score);
        food.setTitle(title);
        food.setContent(content);
        food.setVisitDate(visitDate);
        food.setAddress(address);
        food.setLatitude(latitude);
        food.setLongitude(longitude);
        food.setDel(false);

        return food;
    }

    //== 변경 메서드 ==///
    public void updateFood(Lists list, List<FoodCategory> foodCategoryList, List<FoodPicture> foodPictures,
                           String storeName, Float score, String title, String content, LocalDate visitDate,
                           String address, Double latitude, Double longitude) {
        if(list != null) {
            changeList(list);
        }

        for (FoodCategory foodCategory : foodCategoryList) {
            foodCategory.changeFood(this);
        }

        if(foodPictures != null) {
            for (FoodPicture foodPicture : foodPictures) {
                foodPicture.changeFood(this);
            }
        }

        this.storeName = storeName;
        this.score = score;
        this.title = title;
        this.content = content;
        this.visitDate = visitDate;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
