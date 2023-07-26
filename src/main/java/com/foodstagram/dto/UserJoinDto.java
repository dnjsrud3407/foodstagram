package com.foodstagram.dto;

import com.foodstagram.controller.form.UserJoinForm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserJoinDto {

    private String loginId;

    private String password;

    private String email;

    public UserJoinDto(UserJoinForm form) {
        loginId = form.getLoginId();
        password = form.getPassword();
        email = form.getEmail();
    }
}
