package com.foodstagram.api;

import com.foodstagram.config.redis.RedisService;
import com.foodstagram.controller.form.EmailCheckForm;
import com.foodstagram.controller.validation.ValidationSequence;
import com.foodstagram.error.ErrorResult;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final MailService mailService;
    private final RedisService redisService;
    private final MessageSource ms;

    /**
     * 이메일 인증 메일 보내기
     * - 메일 보낸 후 Redis 에 "이메일" : "인증번호" 저장하기
     * @return
     */
    @PostMapping("/mail/email")
    public ResponseEntity emailSend(@RequestBody Map<String, String> requestMap) {
        String email = requestMap.get("email");

        // 메일 보내기
        String authNum = "";
        try {
            authNum = mailService.sendMail(email);
        } catch (MessagingException e) {
            ErrorResult errorResult = new ErrorResult("email", ms.getMessage("error", null, null));
            return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        }

        // 이메일 전송 완료
        // Redis 저장 및 성공 return
        redisService.saveEmailAuthNum(email, authNum);

        HashMap<String, String> result = new HashMap<>();
        result.put(email, authNum);
        return new ResponseEntity(result, HttpStatus.OK);
    }

    /**
     * 이메일 인증번호 Redis 와 동일한지 확인
     * @return
     */
    @PostMapping("/mail/email/check")
    public ResponseEntity emailCheck(@RequestBody @Validated(ValidationSequence.class) EmailCheckForm emailCheckForm, BindingResult result) {
        // 유효성 검사
        if(result.hasErrors()) {
            if(result.hasFieldErrors("email")) {
                ErrorResult errorResult = new ErrorResult("email", result.getFieldError("email").getDefaultMessage());
                return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
            } else if(result.hasFieldErrors("authNum")) {
                ErrorResult errorResult = new ErrorResult("authNum", result.getFieldError("authNum").getDefaultMessage());
                return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
            }
        }

        String email = emailCheckForm.getEmail();
        String authNum = emailCheckForm.getAuthNum();

        // 인증번호 유효성 검사
        String redisAuthNum = redisService.getValue(email);
        if(redisAuthNum == null || (redisAuthNum != null && !authNum.equals(redisAuthNum))) {
            ErrorResult errorResult = new ErrorResult("authNum", ms.getMessage("equal.emailCheckForm.authNum", null, null));
            return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
        }

        // 세션 정보와 동일하다면 성공 return
        HashMap<String, String> serviceResult = new HashMap<>();
        serviceResult.put("status", "ok");
        return new ResponseEntity(serviceResult, HttpStatus.OK);
    }
}