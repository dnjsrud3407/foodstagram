package com.foodstagram.config.redis;

import com.foodstagram.config.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * RedisTemplate를 활용한 RedisService
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void setValues(String key, String data) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data);
    }

    public void setValues(String key, String data, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    /**
     * refresh 토큰 저장 key: loginId | value: refreshToken
     * @param loginId
     * @param refreshToken
     */
    public void saveRefreshToken(String loginId, String refreshToken) {
        setValues(loginId, refreshToken, Duration.ofMillis(JwtProperties.REFRESH_TOKEN_EXPIRATION_TIME));
    }

    /**
     * email 인증시 인증번호 저장 key: email | value: authNum
     * @param email
     * @param authNum
     */
    public void saveEmailAuthNum(String email, String authNum) {
        setValues(email, authNum, Duration.ofMinutes(10));
    }

    public String getValue(String key) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();

        return values.get(key) != null ? (String) values.get(key) : null;
    }

    /**
     * refresh 토큰 가져오기 key: loginId
     * @param loginId
     * @return
     */
    public String getRefreshToken(String loginId) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();

        return values.get(loginId) != null ? (String) values.get(loginId) : null;
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 이메일 인증번호 확인 완료시 삭제하기
     * @param email
     */
    public void deleteEmailAuthNum(String email) {
        redisTemplate.delete(email);
    }

    /**
     * Redis에 key-value가 존재하는지 확인
     * @param key
     * @param value
     * @return
     */
    public boolean isExist(String key, String value) {
        return getValue(key) == value;
    }

}
