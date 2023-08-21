package com.foodstagram.controller;

import com.foodstagram.config.auth.PrincipalDetails;
import com.foodstagram.config.jwt.JwtTokenService;
import com.foodstagram.config.redis.RedisService;
import com.foodstagram.controller.form.UserChangePwForm;
import com.foodstagram.controller.form.UserJoinForm;
import com.foodstagram.controller.validation.ValidationSequence;
import com.foodstagram.dto.UserDto;
import com.foodstagram.dto.UserJoinDto;
import com.foodstagram.entity.User;
import com.foodstagram.error.ErrorResult;
import com.foodstagram.service.FoodService;
import com.foodstagram.service.ListService;
import com.foodstagram.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final FoodService foodService;
    private final ListService listService;
    private final BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/myPage")
    public String login(@AuthenticationPrincipal PrincipalDetails principalDetails, Model model) {
        User user = principalDetails.getUser();
        Long userId = user.getId();

        UserDto userDto = new UserDto(userId, user.getLoginId(), user.getEmail(), user.getOauth());
        Long countFoods = foodService.countFoods(userId);
        Long countLists = listService.countLists(userId);

        model.addAttribute("user", userDto);
        model.addAttribute("countFoods", countFoods);
        model.addAttribute("countLists", countLists);

        return "user/myPage";
    }

    @GetMapping("/changePw")
    public String changePwForm(Model model) {
        model.addAttribute("userChangePwForm", new UserChangePwForm());

        return "user/changePw";
    }

    @PostMapping("/changePw")
    public String changePw(@Validated(ValidationSequence.class) @ModelAttribute UserChangePwForm userChangePwForm, BindingResult result,
                           @AuthenticationPrincipal PrincipalDetails principalDetails) {
        User user = principalDetails.getUser();
        // 유효성 확인
        validate(userChangePwForm, result, user);

        if(result.hasErrors()) {
            result.addError(new ObjectError("userChangePwForm", null, null, "error"));
            return "user/changePw";
        }

        userService.changePassword(user.getId(), userChangePwForm.getNewPassword());

        return "redirect:/user/myPage";
    }

    /**
     * 유효성 검사
     * - db와 비밀번호 일치한지 확인
     * - 새 비밀번호 동일한지 확인
     * @param userChangePwForm
     * @param result
     * @param user
     */
    private void validate(UserChangePwForm userChangePwForm, BindingResult result, User user) {
        String password = user.getPassword();
        if(!passwordEncoder.matches(userChangePwForm.getPassword(), password)) {
            result.rejectValue("password", "equal");
        }

        if(!userChangePwForm.getNewPassword().equals(userChangePwForm.getNewPasswordConfirm())) {
            result.rejectValue("newPasswordConfirm", "equal");
        }
    }

    /**
     * 회원 탈퇴하기
     * @return
     */
    @GetMapping("/delete")
    public String deleteForm() {
        return "user/deleteForm";
    }

    /**
     * 회원 탈퇴하기
     * - 탈퇴 후 로그아웃 하기
     * @param deleteCheck
     * @param principalDetails
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/delete")
    public String delete(@RequestParam(name = "deleteCheck") Boolean deleteCheck,
                         @AuthenticationPrincipal PrincipalDetails principalDetails, HttpServletRequest request, HttpServletResponse response) {
        log.info("deleteCheck: {}", deleteCheck);
        // 탈퇴 동의 미체크 시
        if(!deleteCheck) {
            return "user/deleteForm";
        }

        User user = principalDetails.getUser();

        // 탈퇴 작업
        userService.deleteUser(user.getId());

        // 로그아웃 하기
        return "redirect:/logout";
    }

}
