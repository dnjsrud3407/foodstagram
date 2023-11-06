package com.foodstagram.config.jwt;

public interface JwtProperties {

    int ACCESS_TOKEN_EXPIRATION_TIME = 1000*60*10; // 10분
    int REFRESH_TOKEN_EXPIRATION_TIME = 1000*60*60*24*14; // 14일
    int REFRESH_TOKEN_NEED_RECREATE_TIME = 1000*60*60*24*7; // 7일
    int COOKIE_MAX_AGE = 1000*60*60*24*14; // 14일
    String ACCESS_TOKEN_COOKIE_NAME = "access";
    String REFRESH_TOKEN_COOKIE_NAME = "refresh";

}