package com.foodstagram.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.foodstagram.config.redis.RedisService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final RedisService redisService;

    /**
     * Access 토큰 발급
     * @param loginId
     * @return
     */
    public String createAccessToken(String loginId) {
        String jwtToken = JWT.create()
                .withSubject(JwtProperties.ACCESS_TOKEN_COOKIE_NAME)
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.ACCESS_TOKEN_EXPIRATION_TIME)) // 만료시간
                .withClaim("loginId", loginId)
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        return jwtToken;
    }

    /**
     * Refresh 토큰 발급
     * @return
     */
    public String createRefreshToken() {
        String jwtToken = JWT.create()
                .withSubject(JwtProperties.REFRESH_TOKEN_COOKIE_NAME)
                .withExpiresAt(new Date(System.currentTimeMillis() + JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME)) // 만료시간
                .sign(Algorithm.HMAC512(JwtProperties.SECRET));

        return jwtToken;
    }

    /**
     * expresAt < 현재시간 + 7일
     * Refresh 토큰 재발급 필요한지 확인
     * @param jwtToken
     * @return
     */
    public boolean isNeedReCreate(String jwtToken) {
        Date expiresAt = JWT.decode(jwtToken).getExpiresAt();

        return expiresAt.before(new Date(System.currentTimeMillis() + JwtProperties.REFRESH_TOKEN_NEED_RECREATE_TIME));
    }

    /**
     * 토큰 만료됐는지 확인, 조작되었는지도 함께 확인 가능
     * @param jwtToken
     * @return
     */
    public boolean isExpired(String jwtToken) throws JWTDecodeException {
        Date expiresAt = JWT.decode(jwtToken).getExpiresAt();
        return expiresAt.before(new Date(System.currentTimeMillis()));
    }

    /**
     * 토큰 값이 조작되지 않았는지 확인
     * @param jwtToken
     * @throws JWTDecodeException
     */
    public void checkValid(String jwtToken) throws JWTDecodeException {
        JWT.decode(jwtToken);
    }

    /**
     * Redis에 저장된 refresh 토큰과 쿠키의 refresh 토큰이 동일한지 확인
     * @param loginId
     * @param refreshToken
     */
    public void checkEqualRedis(String loginId, String refreshToken) {
        String redisRefreshToken = redisService.getRefreshToken(loginId);
        if(!redisRefreshToken.equals(refreshToken)) {
            throw new JWTVerificationException("유효하지 않은 Refresh Token 입니다.");
        }
    }

    /**
     * 토큰에서 loginId 가져오기
     * @param jwtToken
     * @return
     */
    public String getLoginId(String jwtToken) {
        String loginId = JWT.decode(jwtToken).getClaim("loginId").asString();

        if(loginId == null || loginId.isEmpty()) {
            throw new JWTVerificationException("유효하지 않은 Access Token 입니다.");
        }

        return loginId;
    }

    /**
     * 토큰 정보가 담긴 쿠키 생성
     * @param cookieName
     * @param jwtToken
     * @return
     */
    public Cookie createCookieToken(String cookieName, String jwtToken) throws UnsupportedEncodingException {
        Cookie tokenCookie = CookieUtil.createTokenCookie(cookieName, jwtToken, JwtProperties.COOKIE_MAX_AGE);
        return tokenCookie;
    }

    /**
     * Redis 에 저장된 refresh 토큰 값 변경
     * @param loginId
     * @param refreshToken
     */
    public void changeRefreshToken(String loginId, String refreshToken) {
        redisService.saveRefreshToken(loginId, refreshToken);
    }

    /**
     * 토큰 만료시키기
     * @param request
     * @param response
     */
    public void expireToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie accessTokenCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(JwtProperties.ACCESS_TOKEN_COOKIE_NAME))
                .findFirst().orElse(null);

        Cookie refreshTokenCookie = Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(JwtProperties.REFRESH_TOKEN_COOKIE_NAME))
                .findFirst().orElse(null);

        if(accessTokenCookie != null) CookieUtil.expireCookie(accessTokenCookie, response);

        if(refreshTokenCookie != null) CookieUtil.expireCookie(refreshTokenCookie, response);
    }
}
