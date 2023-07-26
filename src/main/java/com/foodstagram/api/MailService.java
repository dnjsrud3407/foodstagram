package com.foodstagram.api;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSenderImpl javaMailSender;

    private int makeRandomNumber() {
        Random random = new Random();
        return random.nextInt(888888) + 111111;
    }

    public String sendMail(String email) throws MessagingException {
        int emailConfirmNum = makeRandomNumber();

        String title = "[foodstagram] 푸드스타그램 회원가입 인증";
        String message =
                "foodstagram 회원가입을 환영합니다." +
                "<br><br>" +
                "인증번호는 " + emailConfirmNum + " 입니다." +
                "<br>" +
                "해당 인증번호를 인증번호 확인란에 기입하여 주세요.";

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
        helper.setTo(email);
        helper.setSubject(title);
        helper.setText(message, true);
        javaMailSender.send(mimeMessage);

        return Integer.toString(emailConfirmNum);
    }
}