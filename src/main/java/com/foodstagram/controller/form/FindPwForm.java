package com.foodstagram.controller.form;

import com.foodstagram.controller.validation.ValidationGroups;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindPwForm {

    @NotBlank(message = "아이디를 입력해주세요.", groups = ValidationGroups.NotBlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9-_]{5,20}$", message = "아이디 형식이 맞지 않습니다.",
            groups = ValidationGroups.PatternGroup.class)
    private String loginId;

    @NotBlank(message = "이메일을 입력해주세요.", groups = ValidationGroups.NotBlankGroup.class)
    @Email(regexp = "^[a-zA-Z0-9-_]+@[a-z]+\\.[a-z]{2,3}$", message = "이메일 형식이 맞지 않습니다.",
            groups = ValidationGroups.PatternGroup.class)
    private String email;

    @NotBlank(message = "인증번호를 입력해주세요.", groups = ValidationGroups.NotBlankGroup.class)
    @Size(min = 6, max = 6, message = "인증번호 형식이 맞지 않습니다.",
            groups = ValidationGroups.PatternGroup.class)
    private String authNum;

}
