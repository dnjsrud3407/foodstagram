package com.foodstagram.config.oauth;

import com.foodstagram.config.auth.PrincipalDetails;
import com.foodstagram.config.jwt.JwtProperties;
import com.foodstagram.config.jwt.JwtTokenService;
import com.foodstagram.config.redis.RedisService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 로그인이 성공하면 JWT 토큰을 생성하고 쿼리 파라미터로 전송.
 * 성공 후 JwtAuthorizationFilter 이동해서 쿠키 저장한다.
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

        // refresh Token Redis에 저장
        redisService.saveRefreshToken(principalDetails.getUser().getLoginId(), refreshToken);

        // access, refresh Token 을 파라미터로 전송한다
        String uri = createURI(accessToken, refreshToken);
        getRedirectStrategy().sendRedirect(request, response, uri);
    }

    private String createURI(String accessToken, String refreshToken) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.set(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, accessToken);
        queryParams.set(JwtProperties.REFRESH_TOKEN_COOKIE_NAME, refreshToken);

        // TODO: scheme, host 변경하기
        return UriComponentsBuilder
                .newInstance()
                .scheme("http")
                .host("localhost")
                .port("8080")
                .path("/oauth2Success")
                .queryParams(queryParams)
                .build()
                .toUriString();
    }
}
