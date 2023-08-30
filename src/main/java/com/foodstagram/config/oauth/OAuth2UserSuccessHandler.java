package com.foodstagram.config.oauth;

import com.foodstagram.config.auth.PrincipalDetails;
import com.foodstagram.config.jwt.JwtProperties;
import com.foodstagram.config.jwt.JwtTokenService;
import com.foodstagram.config.redis.RedisService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * OAuth2 로그인이 성공하면 JWT 토큰을 생성하고 Redis에 저장
 * 메인 페이지로 이동한다
 */
@Component
@RequiredArgsConstructor
public class OAuth2UserSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenService jwtTokenService;
    private final RedisService redisService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        // access Token, refresh Token 생성 : HMAC 암호방식(secret 키 사용)
        String accessToken = jwtTokenService.createAccessToken(principalDetails.getUser().getLoginId());
        String refreshToken = jwtTokenService.createRefreshToken();

        // Token cookie 에 저장
        Cookie accessTokencookie = jwtTokenService.createCookieToken(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, accessToken);
        Cookie refreshTokencookie = jwtTokenService.createCookieToken(JwtProperties.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        response.addCookie(accessTokencookie);
        response.addCookie(refreshTokencookie);

        // refresh Token Redis에 저장
        redisService.saveRefreshToken(principalDetails.getUser().getLoginId(), refreshToken);

        response.sendRedirect("/");
    }

}
