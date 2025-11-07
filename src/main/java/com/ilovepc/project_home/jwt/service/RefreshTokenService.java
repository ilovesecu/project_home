package com.ilovepc.project_home.jwt.service;

import com.ilovepc.project_home.config.redis.RedisService;
import com.ilovepc.project_home.config.redis.vo.RedisNamespace;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisService redisService;
    private static final String RT_KEY_PREFIX = "RT:"; //redis key prefix 입니다~

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    // RefreshToken Redis 저장
    public void saveToken(String username, String refreshToken){
        redisService.saveRedisData(RedisNamespace.REFRESH_TOKEN.getNamespace(), username, refreshToken, refreshTokenValidity);
    }
    // RefreshToken Redis 조회
    public String findToken(String username){
        return redisService.getRedisData(RedisNamespace.REFRESH_TOKEN.getNamespace(), username, String.class);
    }
    // RefreshToken Redis 삭제 - 로그아웃
    public void deleteToken(String username){
        redisService.delteRedisData(RedisNamespace.REFRESH_TOKEN.getNamespace(), username);
    }
}
