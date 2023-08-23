package com.foodstagram.controller;

import com.foodstagram.api.MailService;
import com.foodstagram.config.redis.RedisService;
import com.foodstagram.controller.form.EmailCheckForm;
import com.foodstagram.controller.form.FindPwForm;
import com.foodstagram.controller.form.UserJoinForm;
import com.foodstagram.controller.validation.ValidationSequence;
import com.foodstagram.dto.UserJoinDto;
import com.foodstagram.error.ErrorResult;
import com.foodstagram.service.UserService;
import jakarta.mail.MessagingException;
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
    private final MailService mailService;
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

        validateAuthNum(userJoinForm.getEmail(), userJoinForm.getAuthNum(), result);
    }

    /**
     * 이메일 인증번호 동일한지 확인
     * @param email
     * @param authNum
     * @param result
     */
    private void validateAuthNum(String email, String authNum, BindingResult result) {
        String redisAuthNum = redisService.getValue(email);

        if(redisAuthNum != null) {
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
        validateAuthNum(emailCheckForm.getEmail(), emailCheckForm.getAuthNum(), result);

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

        if(flashMap == null || flashMap.get("loginId") == null) {
            return "redirect:/account/loginForm";
        }

        String loginId = flashMap.get("loginId").toString();
        model.addAttribute("loginId", loginId);

        return "account/findLoginIdResult";
    }

    /**
     * 비밀번호 찾기
     * @return
     */
    @GetMapping("/findPw")
    public String findPwForm(Model model) {
        model.addAttribute("findPwForm", new FindPwForm());

        return "account/findPw";
    }

    @PostMapping("/findPw")
    public String findPw(@Validated(ValidationSequence.class) @ModelAttribute FindPwForm findPwForm, BindingResult result,
                         RedirectAttributes redirectAttributes) {
        // 1. 유효성 검사 - Redis 에 이메일 인증번호 동일한지 확인
        validateAuthNum(findPwForm.getEmail(), findPwForm.getAuthNum(), result);

        // 2. 유효성 검사
        if(result.hasErrors()) {
            result.addError(new ObjectError("findPwForm", null, null, "error"));
            return "account/findPw";
        }

        // 3. 유효성 검사 - 일치하는 loginId, email이 있는지 체크
        String loginId = findPwForm.getLoginId();
        String email = findPwForm.getEmail();

        try {
            userService.findPassword(loginId, email);
        } catch (IllegalStateException e) {
            String message = e.getMessage();

            if(message.contains("loginId")) {
                result.rejectValue("loginId", "notFound");
            } else if(message.contains("email")) {
                result.rejectValue("email", "notFound");
            } else {
                result.rejectValue("loginId", "notFoundUser");
            }
        }

        if(result.hasErrors()) {
            result.addError(new ObjectError("findPwForm", null, null, "error"));
            return "account/findPw";
        }

        // 임시 비밀번호 발급
        String randomPassword = "";
        try {
            randomPassword = mailService.sendPwChangeMail(email);
        } catch (MessagingException e) {
            result.addError(new ObjectError("findPwForm", null, null, "error"));
            return "account/findPw";
        }

        userService.changePassword(loginId, email, randomPassword);

        // redis 에서 삭제
        redisService.deleteEmailAuthNum(email);

        redirectAttributes.addFlashAttribute("email", email);

        return "redirect:/account/findPwResult";
    }

    /**
     * 비밀번호 찾기 결과
     * @return
     */
    @GetMapping("/findPwResult")
    public String findPwResult(HttpServletRequest request, Model model) {
        Map<String, ?> flashMap = RequestContextUtils.getInputFlashMap(request);

        if(flashMap == null || flashMap.get("email") == null) {
            return "redirect:/account/loginForm";
        }

        String loginId = flashMap.get("email").toString();
        model.addAttribute("email", loginId);

        return "account/findPwResult";
    }
}
