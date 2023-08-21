package com.foodstagram.controller;

import com.foodstagram.config.redis.RedisService;
import com.foodstagram.controller.form.EmailCheckForm;
import com.foodstagram.controller.form.UserJoinForm;
import com.foodstagram.controller.validation.ValidationSequence;
import com.foodstagram.dto.UserJoinDto;
import com.foodstagram.error.ErrorResult;
import com.foodstagram.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final UserService userService;
    private final RedisService redisService;
    private final MessageSource ms;

    /**
     * 로그인 페이지
     * @return
     */
    @GetMapping("/loginForm")
    public String loginForm() {
        return "account/loginForm";
    }

    /**
     * 회원가입하기
     * @param model
     * @return
     */
    @GetMapping("/join")
    public String joinForm(Model model) {
        model.addAttribute("userJoinForm", new UserJoinForm());

        return "account/joinForm";
    }

    @PostMapping("/join")
    public String join(@Validated(ValidationSequence.class) @ModelAttribute UserJoinForm userJoinForm, BindingResult result) {
        // 1. 유효성 검사 - 비밀번호 동일한지 확인, Redis 에 이메일 인증번호 동일한지 확인
        validate(userJoinForm, result);

        // 2. 유효성 검사
        if(result.hasErrors()) {
            result.addError(new ObjectError("userJoinForm", null, null, "error"));
            return "account/joinForm";
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
            return "account/joinForm";
        }

        // redis 에서 삭제
        redisService.deleteEmailAuthNum(userJoinForm.getEmail());

        return "redirect:/account/loginForm";
    }

    /**
     * 회원가입 유효성 검사
     * - 비밀번호 동일한지 확인
     * - 이메일 인증번호 동일한지 확인
     * @param userJoinForm
     * @param result
     */
    private void validate(UserJoinForm userJoinForm, BindingResult result) {
        if(!userJoinForm.getPassword().equals(userJoinForm.getPasswordConfirm())) {
            result.rejectValue("passwordConfirm", "equal");
        }

        String email = userJoinForm.getEmail();
        String redisAuthNum = redisService.getValue(email);

        if(redisAuthNum != null) {
            String authNum = userJoinForm.getAuthNum();
            if(!authNum.equals(redisAuthNum)) {
                result.rejectValue("authNum", "equal");
            }
        } else {
            result.rejectValue("authNum", "expired");
        }
    }

    /**
     * 회원가입시 아이디 중복 확인
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
     * 회원가입시 이메일 중복 확인
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

    /**
     * loginId 찾기
     * @param model
     * @return
     */
    @GetMapping("/findLoginId")
    public String findLoginIdForm(Model model) {
        model.addAttribute("emailCheckForm", new EmailCheckForm());

        return "account/findLoginId";
    }

    @PostMapping("/findLoginId")
    public String findLoginId(@Validated(ValidationSequence.class) @ModelAttribute EmailCheckForm emailCheckForm, BindingResult result,
                              RedirectAttributes redirectAttributes) {
        // 1. 유효성 검사 - Redis 에 이메일 인증번호 동일한지 확인
        validate(emailCheckForm, result);

        // 2. 유효성 검사
        if(result.hasErrors()) {
            result.addError(new ObjectError("emailCheckForm", null, null, "error"));
            return "account/findLoginId";
        }

        // 3. 유효성 검사 - 일치하는 loginId가 있는지 체크
        String loginId = "";
        try {
            loginId = userService.findLoginId(emailCheckForm.getEmail());
        } catch (IllegalStateException e) {
            result.rejectValue("email", "notFound");
        }

        if(result.hasErrors()) {
            result.addError(new ObjectError("emailCheckForm", null, null, "error"));
            return "account/findLoginId";
        }

        // redis 에서 삭제
        redisService.deleteEmailAuthNum(emailCheckForm.getEmail());

        // loginId 3~4자리는 *로 변경
        loginId = makeLoginIdUnknown(loginId);
        redirectAttributes.addFlashAttribute("loginId", loginId);

        return "redirect:/account/findLoginIdResult";
    }

    /**
     * loginId 찾을 때 유효성 검사
     * - 이메일 인증번호 동일한지 확인
     * @param emailCheckForm
     * @param result
     */
    private void validate(EmailCheckForm emailCheckForm, BindingResult result) {
        String email = emailCheckForm.getEmail();
        String authNum = emailCheckForm.getAuthNum();
        String redisAuthNum = redisService.getValue(email);

        if(redisAuthNum == null || (redisAuthNum != null && !authNum.equals(redisAuthNum))) {
            result.rejectValue("authNum", "equal");
        }
    }

    private String makeLoginIdUnknown(String loginId) {
        return loginId.substring(0, 3) + "**" + loginId.substring(5, loginId.length());
    }

    /**
     * loginId 찾기 결과
     * @param request
     * @param model
     * @return
     */
    @GetMapping("/findLoginIdResult")
    public String findLoginIdResult(HttpServletRequest request, Model model) {
        Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(request);

        if(flashMap == null) {
            return "redirect:/account/loginForm";
        }

        String loginId = flashMap.get("loginId").toString();
        model.addAttribute("loginId", loginId);

        return "account/findLoginIdResult";
    }
}
