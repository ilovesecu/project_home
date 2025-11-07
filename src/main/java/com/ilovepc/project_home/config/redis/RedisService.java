package com.ilovepc.project_home.config.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.config.redis.vo.RedisNamespace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public void saveRedisData(String key, Object value, long ttl) {
        saveRedisData(RedisNamespace.HOME_DEFAULT.getNamespace(), key, value, ttl);
    }

    public void saveRedisData(String namespace, String key, Object value, long ttl){
        try{
            redisTemplate.opsForValue().set(
                    namespace + key,
                    toJson(value),
                    ttl,
                    TimeUnit.MILLISECONDS
            );
        }catch (Exception e){
            log.error("saveRedisData FAIL! nameSpace:{}, key:{}", namespace, key, e);
        }
    }

    public Object getRedisData(String namespace, String key){
        Object redisData = null;
        try{
            redisData = redisTemplate.opsForValue().get(namespace + key);
        }catch (Exception e){
            log.error("getRedisData FAIL! nameSpace:{}, key:{}", namespace, key, e);
        }
        return redisData;
    }

    //레디스에 저장된 데이터 반환
    public <T> T getRedisData(String namespace, String key, Class<T> clazz){
        Object redisData = getRedisData(namespace, key);
        if(ObjectUtils.isEmpty(redisData)){
            log.error("getRedisData FAIL! nameSpace:{}, key:{}", namespace, key);
            return null;
        }

        if(clazz.isInstance(redisData)){
            try {
                // clazz.cast() : obj를 clazz 타입으로 안전하게 캐스팅
                return clazz.cast(redisData);
            } catch (ClassCastException e) {
                log.warn("getRedisData - Mismatched type even after isInstance check. namespace:{} Key: {}",namespace, key, e);
                return null;
            }
        }

        // 데이터가 JSON 문자열인 경우 (가장 흔한 케이스)
        // (e.g. UserDto.class 요청, JSON String 저장됨)
        if (redisData instanceof String) {
            try {
                String jsonString = (String) redisData;
                // ObjectMapper를 사용해 JSON 문자열을 T 클래스 객체로 변환
                return objectMapper.readValue(jsonString, clazz);
            } catch (JsonProcessingException e) {
                log.error("getRedisData - Failed to deserialize JSON to {}. Key: {}, JSON: {}",
                        clazz.getSimpleName(), key, redisData, e);
                return null;
            }
        }
        //호환되지 않는 타입일 경우
        log.error("getRedisData - Incompatible types. Data in Redis is {} but requested {}. Key: {}",
                redisData.getClass().getName(), clazz.getName(), key);
        return null;
    }

    //레디스에서 데이터 삭제
    public void delteRedisData(String namespace, String key){
        try{
            redisTemplate.delete(namespace + key);
        }catch (Exception e){
            log.error("delteRedisData FAIL! nameSpace:{}, key:{}", namespace, key, e);
        }
    }

    private String toJson(Object value){
        if(value == null) return null;
        try{
            return objectMapper.writeValueAsString(value);
        }catch (JsonProcessingException e){
            log.error("Failed to serialize object to JSON! : {}",value, e);
            throw new RuntimeException("JSON Serialize Fail! ",e);
        }
    }
}
