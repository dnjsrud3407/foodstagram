package com.foodstagram.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodstagram.config.auth.PrincipalDetails;
import com.foodstagram.config.redis.RedisService;
import com.foodstagram.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * spring security 에서 /login 요청 시 동작하는 필터
 * 로그인이 성공하면 session에 authentication 객체를 저장하고,
 * JWT 토큰을 생성한다.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final MessageSource ms;
    private final JwtTokenService jwtTokenService;
    private final RedisService redisService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 1. loginId, password 받기
        ObjectMapper mapper = new ObjectMapper();
        User user = null;
        try {
            user = mapper.readValue(request.getInputStream(), User.class);
        } catch (IOException e) {
            throw new InternalAuthenticationServiceException(ms.getMessage("notFound.user", null, null));
        }

        String loginId = user.getLoginId();
        String password = user.getPassword();

        // 유저 토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginId, password);

        // 2. 로그인 시도하기
        //    -> authenticationManager를 통해 로그인 시도를 하면
        //       PrincipalDetailsService.loadUserByUsername 이 실행된다.
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. authentication을 return 하지만 session.stateless 이기 때문에 저장되지 않음
        return authentication;
    }

    /**
     * attemptAuthentication 실행 후 인증이 정상적으로 되었으면 successfulAuthentication 함수가 실행됨.
     * JWT 토큰을 만들어서 cookie 에 저장함.
     * 성공 후 JwtAuthorizationFilter 이동
     * @param authResult the object returned from the <tt>attemptAuthentication</tt>
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException {
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();

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

        // 로그인 성공 응답
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sucess", true);
        response.getWriter().println(jsonObject);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        // 로그인 실패 응답
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sucess", false);

        // 아이디, 비밀번호가 틀린 경우
        if(failed instanceof BadCredentialsException) {
            jsonObject.put("message", ms.getMessage("notFound.user", null, null));
        } else {
            jsonObject.put("message", failed.getMessage());
        }

        response.getWriter().println(jsonObject);
    }
}
