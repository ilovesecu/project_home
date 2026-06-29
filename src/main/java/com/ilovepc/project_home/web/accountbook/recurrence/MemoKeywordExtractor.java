package com.ilovepc.project_home.web.accountbook.recurrence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 수동 메모나 간단 메모에서 반복성 판단의 단서가 되는 핵심 키워드만 추출하는 역할을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class MemoKeywordExtractor {
    /**
     * 메모 앞쪽의 분류 태그를 제거합니다.
     *
     * 예시:
     * - "[지출][고정][보험] @성은 보험1 2606" -> " @성은 보험1 2606"
     * - "[지출][식비] @공동 점심" -> " @공동 점심"
     */
    private static final Pattern TAG_BLOCK_PATTERN = Pattern.compile("\\[[^\\]]+]");

    /**
     * '@주체' 토큰을 제거합니다. 주체는 별도 컬럼으로 관리하므로 반복 키에는 넣지 않습니다.
     *
     * 예시:
     * - "@성은 보험1 2606" -> " 보험1 2606"
     * - "@공동 냉장고" -> " 냉장고"
     */
    private static final Pattern OWNER_PATTERN = Pattern.compile("@\\S+");

    /**
     * 메모 본문에 적힌 대상월을 제거합니다. 같은 고정 지출이라도 2605, 2606처럼 월만 바뀌면
     * 서로 다른 키로 갈라지는 것을 막기 위한 패턴입니다.
     *
     * 예시:
     * - "보험1 2606" -> "보험1 "
     * - "급여 2605월" -> "급여 "
     */
    private static final Pattern TARGET_MONTH_PATTERN = Pattern.compile("(?<!\\d)\\d{4}\\s*월?(?!\\d)");

    /**
     * 괄호 안에 적힌 할부 진행 정보를 제거합니다. 회차가 바뀌어도 같은 물건의 반복 지출로 묶기 위함입니다.
     *
     * 예시:
     * - "냉장고(10/12)" -> "냉장고"
     * - "식세기 (3 / 12)" -> "식세기 "
     */
    private static final Pattern INSTALLMENT_PATTERN = Pattern.compile("\\([^)]*\\d+\\s*/\\s*\\d+[^)]*\\)");

    /**
     * '8회차'처럼 본문에 풀어쓴 회차 정보를 제거합니다.
     *
     * 예시:
     * - "엄마 대출상환 8회차" -> "엄마 대출상환 "
     * - "세건 2 회차" -> "세건 "
     */
    private static final Pattern INSTALLMENT_ROUND_PATTERN = Pattern.compile("\\d+\\s*회차");

    /**
     * 과거 메모처럼 '@주체' 없이 사람 이름이 본문 앞에 붙은 경우를 정리하기 위한 단어 목록입니다.
     *
     * 예시:
     * - "성은 보험1" -> "보험1"
     * - "승주 연저펀" -> "연저펀"
     */
    private static final List<String> LEADING_OWNER_WORDS = List.of("공동", "승주", "성은");

    private final AccountBookTextNormalizer textNormalizer;

    /**
     * 태그, 주체, 대상월, 할부 회차처럼 반복 키를 흔들 수 있는 부가 정보를 제거하고 핵심 메모 단어를 반환합니다.
     *
     * 전체 변환 예시:
     * - "[지출][고정][보험] @성은 보험1 2606" -> "보험1"
     * - "[지출] 냉장고(10/12)" -> "냉장고"
     * - "[지출] 엄마 대출상환 8회차" -> "엄마대출상환"
     */
    public String extract(String memo) {
        if (!StringUtils.hasText(memo)) {
            return "";
        }

        // 1. 분류 태그 제거: "[지출][고정][보험] @성은 보험1 2606" -> " @성은 보험1 2606"
        String keyword = TAG_BLOCK_PATTERN.matcher(memo).replaceAll(" ");

        // 2. 주체 제거: " @성은 보험1 2606" -> "  보험1 2606"
        keyword = OWNER_PATTERN.matcher(keyword).replaceAll(" ");

        // 3. 대상월 제거: "보험1 2606" -> "보험1 "
        keyword = TARGET_MONTH_PATTERN.matcher(keyword).replaceAll(" ");

        // 4. 괄호형 할부 회차 제거: "냉장고(10/12)" -> "냉장고"
        keyword = INSTALLMENT_PATTERN.matcher(keyword).replaceAll(" ");

        // 5. 문장형 회차 제거: "엄마 대출상환 8회차" -> "엄마 대출상환 "
        keyword = INSTALLMENT_ROUND_PATTERN.matcher(keyword).replaceAll(" ");

        // 6. '@주체' 없이 앞에 붙은 사람 이름 제거: "성은 보험1" -> "보험1"
        keyword = removeLeadingOwnerWord(keyword.trim());

        // 7. 남은 공백/특수문자를 제거해 최종 키워드로 정규화: "엄마 대출상환 " -> "엄마대출상환"
        return textNormalizer.normalize(keyword);
    }

    /**
     * '@주체' 없이 적힌 예전 메모에서도 '성은 보험1'처럼 앞에 붙은 사람 이름을 제거합니다.
     */
    private String removeLeadingOwnerWord(String keyword) {
        for (String ownerWord : LEADING_OWNER_WORDS) {
            if (keyword.startsWith(ownerWord)) {
                return keyword.substring(ownerWord.length()).trim();
            }
        }

        return keyword;
    }
}
