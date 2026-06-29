package com.ilovepc.project_home.web.accountbook.recurrence;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 거래 적요와 메모를 반복 패턴 비교에 사용할 수 있는 안정적인 문자열로 정리하는 역할을 담당합니다.
 */
@Component
public class AccountBookTextNormalizer {
    private static final List<String> NOISE_WORDS = List.of(
            "체크카드",
            "카드승인",
            "승인",
            "결제",
            "입금",
            "출금"
    );

    /**
     * 공백, 특수문자, 거래 처리용 잡음을 제거해서 같은 의미의 텍스트가 같은 키 조각으로 비교되도록 만듭니다.
     */
    public String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        String normalized = value.trim().replaceAll("\\s+", "");
        for (String noiseWord : NOISE_WORDS) {
            normalized = normalized.replace(noiseWord, "");
        }

        return normalized.replaceAll("[^0-9A-Za-z가-힣]+", "");
    }
}
