package com.foodstagram.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String loginId;

    private String password;

    private String email;

    @OneToMany(mappedBy = "user")
    private List<Lists> lists = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Food> foods = new ArrayList<>();

    private String role; // ROLE_USER, ROLE_ADMIN

    private String oauth;

    private Boolean isDel;

    private void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    private void setEmail(String email) {
        this.email = email;
    }

    private void setRole(String role) {
        this.role = role;
    }

    private void setOauth(String oauth) {
        this.oauth = oauth;
    }

    private void setDel(Boolean del) {
        isDel = del;
    }

    //== 생성 메서드 ==//
    public static User createUser(String loginId, String password, String email) {
        User user = new User();
        user.setLoginId(loginId);
        user.setPassword(password);
        user.setEmail(email);
        user.setRole("ROLE_USER");
        user.setDel(false);

        return user;
    }

    public static User createUser(String loginId, String password, String email, String provider) {
        User user = new User();
        user.setLoginId(loginId);
        user.setPassword(password);
        user.setEmail(email);
        user.setRole("ROLE_USER");
        user.setOauth(provider);
        user.setDel(false);

        return user;
    }

    //== 변경 메서드 ==///
    public void changePassword(String password) {
        this.password = password;
    }

    public void deleteUser() {
        this.loginId = null;
        this.password = null;
        this.email = null;
        this.isDel = true;
    }

}
