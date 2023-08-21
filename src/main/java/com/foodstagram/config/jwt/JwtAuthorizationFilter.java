package com.foodstagram.config.jwt;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.foodstagram.config.auth.PrincipalDetails;
import com.foodstagram.entity.User;
import com.foodstagram.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * security 필터 중 BasicAuthenticationFilter 가 있음 - 권한이나 인증이 필요한 특정 주소를 요청했을 때 거치는 필터.
 */
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    private MessageSource ms;
    private JwtTokenService jwtTokenService;
    private UserRepository userRepository;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, MessageSource ms, JwtTokenService jwtTokenService, UserRepository userRepository) {
        super(authenticationManager);
        this.ms = ms;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String requestURI = request.getRequestURI();

        // oauth2 로그인 성공한 경우 -> 쿠키 저장 후 메인 페이지 이동
        if(requestURI.equals("/oauth2Success")) {
            oauth2LoginSuccess(request, response);

            response.sendRedirect("/");
            return;
        }

        try {
            System.out.println("인증이나 권한이 필요한 주소 요청");

            // cookie에서 jwt 토큰 가져옴
            Cookie accessTokenCookie = null;
            Cookie refreshTokenCookie = null;

            // accessToken, refreshToken 이 없는 경우 -> 로그인 페이지 이동
            if (request.getCookies() != null) {
                accessTokenCookie = Arrays.stream(request.getCookies())
                        .filter(cookie -> cookie.getName().equals(JwtProperties.ACCESS_TOKEN_COOKIE_NAME))
                        .findFirst().orElseThrow(() -> new JWTVerificationException(ms.getMessage("verification.accessToken", null, null)));

                refreshTokenCookie = Arrays.stream(request.getCookies())
                        .filter(cookie -> cookie.getName().equals(JwtProperties.REFRESH_TOKEN_COOKIE_NAME))
                        .findFirst().orElseThrow(() -> new JWTVerificationException(ms.getMessage("verification.refreshToken", null, null)));
            } else {
                chain.doFilter(request, response);
                return;
            }

            // refreshTokenCookie 토큰이 조작, 만료되었을 때 -> 로그인 페이지 이동
            String refreshToken = CookieUtil.getAccessToken(refreshTokenCookie);
            if (jwtTokenService.isExpired(refreshToken)) {
                response.sendRedirect("/account/loginForm");
                return;
            }

            // accessToken 이 조작되었다면 -> 로그인 페이지 이동
            String accessToken = CookieUtil.getAccessToken(accessTokenCookie);
            jwtTokenService.checkValid(accessToken);

            // refreshToken 토큰이 쿠키 = Redis 다르다면 -> 로그인 페이지 이동
            String loginId = jwtTokenService.getLoginId(accessToken);
            jwtTokenService.checkEqualRedis(loginId, refreshToken);

            // refreshToken 7일 이내라면 재발급 후 쿠키에 저장, Redis 값 변경
            if(jwtTokenService.isNeedReCreate(refreshToken)) {
                // 이전 쿠키는 만료시킴
                CookieUtil.expireCookie(refreshTokenCookie, response);

                refreshToken = jwtTokenService.createRefreshToken();
                refreshTokenCookie = jwtTokenService.createCookieToken(JwtProperties.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
                response.addCookie(refreshTokenCookie);

                jwtTokenService.changeRefreshToken(loginId, refreshToken);
            }

            // accessToken 이 만료되었다면 재발급 후 쿠키에 저장
            if(jwtTokenService.isExpired(accessToken)) {
                // 이전 쿠키는 만료시킴
                CookieUtil.expireCookie(accessTokenCookie, response);

                accessToken = jwtTokenService.createAccessToken(loginId);
                accessTokenCookie = jwtTokenService.createCookieToken(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, accessToken);
                response.addCookie(accessTokenCookie);
            }

            User user = userRepository.findByLoginId(loginId)
                    .orElseThrow(() -> new InternalAuthenticationServiceException(ms.getMessage("notFound.user", null, null)));

            PrincipalDetails principalDetails = new PrincipalDetails(user);
            Authentication authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());

            // 강제로 security 세션에 접근하여 Authentication 객체를 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);
        } catch (JWTDecodeException e) {
            // 유효한 토큰이 아니라면 JWTDecodeException 발생
            chain.doFilter(request, response);
        } catch (TokenExpiredException e) {
            chain.doFilter(request, response);
        } catch (JWTVerificationException e) {
            // 메인 페이지 요청 시 token 이 없다면(ex-로그아웃 후 메인 페이지 요청) 그대로 메인 페이지 이동
            // 다른 요청이라면 로그인 페이지로 이동
            if(requestURI.equals("/")) {
                chain.doFilter(request, response);
                return;
            }

            response.sendRedirect("/account/loginForm");
        } catch (UnsupportedEncodingException e) {
            chain.doFilter(request, response);
        } catch (Exception e) {
            if(requestURI.equals("/")) {
                chain.doFilter(request, response);
                return;
            }

            log.error(e.getMessage());
            response.sendRedirect("/account/loginForm");
        }

    }

    private void oauth2LoginSuccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String accessToken = request.getParameter(JwtProperties.ACCESS_TOKEN_COOKIE_NAME);
        String refreshToken = request.getParameter(JwtProperties.REFRESH_TOKEN_COOKIE_NAME);

        // Token cookie 에 저장
        Cookie accessTokenCookie = jwtTokenService.createCookieToken(JwtProperties.ACCESS_TOKEN_COOKIE_NAME, accessToken);
        Cookie refreshTokenCookie = jwtTokenService.createCookieToken(JwtProperties.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }
}
