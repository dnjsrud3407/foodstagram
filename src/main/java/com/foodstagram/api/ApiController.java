package com.foodstagram.api;

import com.foodstagram.error.ErrorResult;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {

    private final MailService mailService;
    private final MessageSource ms;

    /**
     * 이메일 인증 메일 보내기
     * - 메일 보낸 후 session에 "이메일" : "인증번호" 저장하기
     * @param session
     * @return
     */
    @PostMapping("/mail/email")
    public ResponseEntity emailSend(@RequestBody Map<String, String> requestMap, HttpSession session) {
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
        // session 생성 및 성공 return
        session.setAttribute(email, authNum);
        session.setMaxInactiveInterval(1800);

        HashMap<String, String> result = new HashMap<>();
        result.put(email, authNum);
        return new ResponseEntity(result, HttpStatus.OK);
    }

    /**
     * 이메일 세션 정보와 동일한지 확인
     * @param requestMap
     * @param session
     * @return
     */
    @PostMapping("/mail/email/check")
    public ResponseEntity emailCheck(@RequestBody Map<String, String> requestMap, HttpSession session) {
        String email = requestMap.get("email");
        String authNum = requestMap.get("authNum");

        // 세션 정보와 동일하지 않다면
        Object authNumSession = session.getAttribute(email);
        if(authNumSession != null) {
            if(!authNum.equals(authNumSession.toString())) {
                ErrorResult errorResult = new ErrorResult("email", ms.getMessage("equal.userJoinForm.authNum", null, null));
                return new ResponseEntity(errorResult, HttpStatus.BAD_REQUEST);
            }
        }

        // 세션 정보와 동일하다면 성공 return
        HashMap<String, String> result = new HashMap<>();
        result.put("status", "ok");
        return new ResponseEntity(result, HttpStatus.OK);
    }
}