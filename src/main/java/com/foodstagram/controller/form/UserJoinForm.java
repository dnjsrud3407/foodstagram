package com.foodstagram.controller.form;

import com.foodstagram.controller.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserJoinForm extends EmailCheckForm {

    @NotBlank(message = "아이디를 입력해주세요.", groups = ValidationGroups.NotBlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9-_]{5,20}$", message = "아이디는 5~20자로 영문 대소문자, 숫자, 특수문자(- 또는 _)만 사용 가능합니다.",
             groups = ValidationGroups.PatternGroup.class)
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요.", groups = ValidationGroups.NotBlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9`~!@#$%^&*()+-_=|:<>,.?/]{8,16}$", message = "비밀번호는 8~16자로 영문 대소문자, 숫자, 특수문자만 사용 가능합니다.",
             groups = ValidationGroups.PatternGroup.class)
    private String password;

    @NotBlank(message = "비밀번호 확인란에 입력해주세요.", groups = ValidationGroups.NotBlankGroup.class)
    private String passwordConfirm;

}
