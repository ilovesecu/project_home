package com.ilovepc.project_home.security.jwt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String RT_KEY_PREFIX = "RT:"; //redis key prefix 입니다~

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    // RefreshToken Redis 저장

    // RefreshToken Redis 조회
    // RefreshToken Redis 삭제 - 로그아웃
}
