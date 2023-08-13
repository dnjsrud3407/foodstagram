package com.foodstagram.config.handler;

import com.foodstagram.config.jwt.CookieUtil;
import com.foodstagram.config.jwt.JwtProperties;
import com.foodstagram.config.jwt.JwtTokenService;
import com.foodstagram.config.redis.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * 로그아웃 성공 후 Redis 의 refresh 토큰 삭제하기
 */
@Component
@RequiredArgsConstructor
public class MyLogoutHandler implements LogoutHandler {

    private final RedisService redisService;
    private final JwtTokenService jwtTokenService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Cookie accessTokenCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(JwtProperties.ACCESS_TOKEN_COOKIE_NAME))
                .findFirst().orElse(null);

        try {
            if(accessTokenCookie != null) {
                String accessToken = CookieUtil.getAccessToken(accessTokenCookie);

                String loginId = jwtTokenService.getLoginId(accessToken);
                redisService.deleteValues(loginId);
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
