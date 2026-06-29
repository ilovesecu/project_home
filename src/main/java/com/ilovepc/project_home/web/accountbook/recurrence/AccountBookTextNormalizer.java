package com.ilovepc.project_home.web.accountbook.recurrence;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 거래 적요와 메모를 반복 패턴 비교에 사용할 수 있는 안정적인 문자열로 정리하는 역할을 담당합니다.
 */
@Component
public class AccountBookTextNormalizer {
    /**
     * SKYLIFE 적요 끝에 붙는 대상월을 제거하기 위한 패턴입니다.
     * 다른 적요의 숫자는 의미가 있을 수 있으므로 SKYLIFE일 때만 이 패턴을 적용합니다.
     *
     * 예시:
     * - "SKYLIFE2603" -> "SKYLIFE"
     * - "SKYLIFE202603" -> "SKYLIFE"
     * - "24시편의점" -> 유지
     */
    private static final Pattern DESCRIPTION_TARGET_MONTH_SUFFIX_PATTERN = Pattern.compile(
            "(?:20\\d{2}(?:0[1-9]|1[0-2])|2\\d(?:0[1-9]|1[0-2]))$"
    );

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

    /**
     * 거래 적요 전용 정규화입니다.
     * 일반 정규화를 먼저 적용한 뒤, SKYLIFE 적요에 한해서 끝에 붙은 청구월만 추가로 제거합니다.
     */
    public String normalizeDescription(String value) {
        String normalized = normalize(value);
        if (!StringUtils.hasText(normalized)) {
            return "";
        }

        if (!normalized.toUpperCase().startsWith("SKYLIFE")) {
            return normalized;
        }

        String withoutTargetMonth = DESCRIPTION_TARGET_MONTH_SUFFIX_PATTERN.matcher(normalized).replaceFirst("");
        if (!StringUtils.hasText(withoutTargetMonth)) {
            return normalized;
        }

        return withoutTargetMonth;
    }
}
