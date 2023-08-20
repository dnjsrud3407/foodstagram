package com.foodstagram.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "list")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Lists extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "list_id")
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "list")
    private List<Food> foods = new ArrayList<>();

    private Boolean isDel;

    private void setName(String name) {
        this.name = name;
    }

    private void setDel(Boolean del) {
        isDel = del;
    }

    //== 연관관계 메서드 ==//
    public void changeUser(User user) {
        this.user = user;
        user.getLists().add(this);
    }

    //== 생성 메서드 ==//
    public static Lists createList(String name, User user) {
        Lists list = new Lists();
        list.setName(name);
        list.changeUser(user);
        list.setDel(false);

        return list;
    }

    //== 변경 메서드 ==//
    public void updateList(String name) {
        this.name = name;
        this.isDel = isDel;
    }
}
