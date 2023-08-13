package com.foodstagram.config.jwt;

import jakarta.servlet.http.Cookie;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class CookieUtil {

    /**
     * 토큰 값을 가진 쿠키 생성
     * @param name
     * @param token
     * @param maxAge
     * @return
     * @throws UnsupportedEncodingException
     */
    public static Cookie createTokenCookie(String name, String token, int maxAge) throws UnsupportedEncodingException {
        Cookie cookie = new Cookie(name, URLEncoder.encode(token, "UTF-8"));
        cookie.setHttpOnly(true);
        // TODO: https 수정
//        cookie.setSecure(true); 
        cookie.setMaxAge(maxAge/1000);
        return cookie;
    }

    /**
     * 쿠키에서 토큰 값 가져오기
     * @param cookie
     * @return
     */
    public static String getAccessToken(Cookie cookie) throws UnsupportedEncodingException {
        return URLDecoder.decode(cookie.getValue(), "UTF-8");
    }
}
