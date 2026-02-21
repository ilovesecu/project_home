package com.ilovepc.project_home.web.dhlottery.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DhlotteryLoginService {
    private final RestTemplate rt = new RestTemplate();
    private final ObjectMapper om = new ObjectMapper();

    public void loginAndGetCookie(String username, String password){
        try{
            log.info("1. RSA 공개키(Modules, Exponent)요청 중.... ");
            String rsaUrl = "https://dhlottery.co.kr/login/selectRsaModulus.do";

            String rsaJsonResponse = rt.getForObject(rsaUrl, String.class);
            JsonNode rootNode = om.readTree(rsaJsonResponse);
            JsonNode dataNode = rootNode.get("data");

            String rsaModulus = dataNode.get("rsaModulus").asText();
            String publicExponent = dataNode.get("publicExponent").asText();

            log.info("2. ID/PW RSA Encrypt...");
            String encryptedId = encryptRSA(username, rsaModulus, publicExponent);
            String encryptedPw = encryptRSA(password, rsaModulus, publicExponent);

            log.info("3. LOGIN PROC....");
            String loginUrl = "https://www.dhlottery.co.kr/login/securityLoginCheck.do";

            // 헤더 설정 (Form-Data 형식 지정)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            // 폼 데이터 세팅 (JS 소스와 동일한 파라미터명 사용)
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("userId", encryptedId);
            params.add("userPswdEncn", encryptedPw);

            // 요청 객체 만들고 전송
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = rt.postForEntity(loginUrl, request, String.class);

            log.info("4. LOGIN SUCCESS");
            // Set-Cookie 헤더에서 쿠키들 빼오기
            List<String> cookies = response.getHeaders().get(HttpHeaders.SET_COOKIE);

            if (cookies != null && !cookies.isEmpty()) {
                System.out.println("====== [ 획득한 세션 쿠키 ] ======");
                for (String cookie : cookies) {
                    System.out.println(cookie);
                }
                System.out.println("==================================");
                // TODO: 획득한 쿠키(JSESSIONID 등)를 전역 변수나 세션에 저장하여
                // 이후 마이페이지 조회나 다른 API 통신 헤더에 'Cookie'로 넣어주면 됩니다!
            } else {
                System.out.println("No cookie");
            }

            log.error("dataNode:{}",dataNode);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private String encryptRSA(String plainText, String hexModulus, String hexExponent) throws Exception {
        // 16진수 문자열을 BigInteger로 변환
        BigInteger modulus = new BigInteger(hexModulus, 16);
        BigInteger exponent = new BigInteger(hexExponent, 16);

        // RSA 공개키 객체 생성
        RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);

        // 암호화 (PKCS1Padding 사용 - JS의 pkcs1pad2 함수와 동일한 역할)
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 암호화된 Byte 배열을 다시 16진수 문자열로 변환 (서버 전송용)
        StringBuilder hexString = new StringBuilder();
        for (byte b : encryptedBytes) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }

    public static void main(String[] args) {
        DhlotteryLoginService a = new DhlotteryLoginService();
        a.loginAndGetCookie("bonobono94", "Wjdtmdwn94!");
    }

}
