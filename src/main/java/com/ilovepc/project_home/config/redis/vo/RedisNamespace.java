package com.ilovepc.project_home.config.redis.vo;

import lombok.Getter;

@Getter
public enum RedisNamespace {
    REFRESH_TOKEN("HOME:RT:"), //refresh token
    HOME_DEFAULT("HOME:DATA:") //기본값
    ; 

    String namespace;

    RedisNamespace(String namespace) {
        this.namespace = namespace;
    }
}
