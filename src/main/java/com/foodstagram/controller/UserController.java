package com.foodstagram.controller;

import com.foodstagram.controller.form.UserJoinForm;
import com.foodstagram.controller.form.UserLoginForm;
import com.foodstagram.controller.validation.ValidationSequence;
import com.foodstagram.dto.UserJoinDto;
import com.foodstagram.error.ErrorResult;
import com.foodstagram.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final MessageSource ms;

    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("userJoinForm", new UserJoinForm());

        return "user/joinForm";
    }

    @PostMapping("/join")
    public String join(@Validated(ValidationSequence.class) @ModelAttribute UserJoinForm userJoinForm, BindingResult result,
                       HttpSession session, Model model) {
        // 1. 유효성 검사 - 비밀번호 동일한지 확인, session에 이메일 인증번호 동일한지 확인
        validate(userJoinForm, result, session);

        // 2. 유효성 검사
        if(result.hasErrors()) {
            result.addError(new ObjectError("userJoinForm", null, null, "error"));
            return "user/joinForm";
        }

        UserJoinDto userJoinDto = new UserJoinDto(userJoinForm);

        // 3. 유효성 검사 - 아이디, 이메일 중복 검사
        // 유효성 통과된다면 회원가입 진행하기
        try{
            userService.join(userJoinDto);
        } catch (IllegalStateException e) {
            String message = e.getMessage();

            String duplicateLoginId = ms.getMessage("duplicate.userJoinForm.loginId", null, null);
            String duplicateEmail = ms.getMessage("duplicate.userJoinForm.email", null, null);

            if(message.equals(duplicateLoginId)) {
                result.rejectValue("loginId", "duplicate");
            } else if(message.equals(duplicateEmail)) {
                result.rejectValue("email", "duplicate");
            }
        }

        if(result.hasErrors()) {
            result.addError(new ObjectError("userJoinForm", null, null, "error"));
            return "user/joinForm";
        }

        return "redirect:/user/login";
    }

    /**
     * 유효성 검사
     * - 비밀번호 동일한지 확인
     * - 이메일 인증번호 동일한지 확인
     * @param userJoinForm
     * @param result
     */
    private void validate(UserJoinForm userJoinForm, BindingResult result, HttpSession session) {
        if(!userJoinForm.getPassword().equals(userJoinForm.getPasswordConfirm())) {
            result.rejectValue("passwordConfirm", "equal");
        }

        Object authNumSession = session.getAttribute(userJoinForm.getEmail());
        if(authNumSession != null) {
            String authNum = userJoinForm.getAuthNum();
            if(!authNum.equals(authNumSession.toString())) {
                result.rejectValue("authNum", "equal");
            }
        } else {
            result.rejectValue("authNum", "expired");
        }
    }

    /**
     * 아이디 중복 확인
     * @param requestMap
     * @return
     */
    @PostMapping("/loginIdCheck")
    public ResponseEntity loginIdCheck(@RequestBody Map<String, String> requestMap) {
        String loginId = requestMap.get("loginId");
        // 아이디 중복 확인
        Long userId = userService.validateDuplicateLoginId(loginId);

        if(userId != null) {
            ErrorResult errorResult = new ErrorResult("loginId", ms.getMessage("duplicate.userJoinForm.loginId", null, null));
            return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        }

        // 사용 가능한 loginId
        HashMap<String, String> result = new HashMap<>();
        result.put("status", "ok");
        return new ResponseEntity(result, HttpStatus.OK);
    }

    /**
     * 이메일 중복 확인
     * @param requestMap
     * @return
     */
    @ResponseBody
    @PostMapping("/emailCheck")
    public ResponseEntity emailCheck(@RequestBody Map<String, String> requestMap) {
        String email = requestMap.get("email");
        // 이메일 중복 확인
        Long userId = userService.validateDuplicateEmail(email);

        if(userId != null) {
            ErrorResult errorResult = new ErrorResult("email", ms.getMessage("duplicate.userJoinForm.email", null, null));
            return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        }

        // 사용 가능한 email
        HashMap<String, String> result = new HashMap<>();
        result.put("status", "ok");
        return new ResponseEntity(result, HttpStatus.OK);
    }


    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("userLoginForm", new UserLoginForm());

        return "user/login";
    }

}
