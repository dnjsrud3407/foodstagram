package com.foodstagram.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    private String name;

    private Boolean isDel;

    private void setName(String name) {
        this.name = name;
    }

    private void setDel(Boolean isDel) {
        this.isDel = isDel;
    }

    //== 생성 메서드 ==//
    public static Category createCategory(String name, Boolean isDel) {
        Category category = new Category();
        category.setName(name);
        category.setDel(isDel);

        return category;
    }

    //== 변경 메서드==//
    public void updateCategory(String name, Boolean isDel) {
        this.name = name;
        this.isDel = isDel;
    }
}
