package com.foodstagram.controller.form;

import com.foodstagram.controller.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserChangePwForm {

    @NotBlank(message = "비밀번호를 입력해주세요.", groups = ValidationGroups.NotBlankGroup.class)
    private String password;

    @NotBlank(message = "새 비밀번호를 입력해주세요.", groups = ValidationGroups.NotBlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9`~!@#$%^&*()+-_=|:<>,.?/]{8,16}$", message = "비밀번호는 8~16자로 영문 대소문자, 숫자, 특수문자만 사용 가능합니다.",
            groups = ValidationGroups.PatternGroup.class)
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인란에 입력해주세요.", groups = ValidationGroups.NotBlankGroup.class)
    @Pattern(regexp = "^[a-zA-Z0-9`~!@#$%^&*()+-_=|:<>,.?/]{8,16}$", message = "비밀번호는 8~16자로 영문 대소문자, 숫자, 특수문자만 사용 가능합니다.",
            groups = ValidationGroups.PatternGroup.class)
    private String newPasswordConfirm;

}
