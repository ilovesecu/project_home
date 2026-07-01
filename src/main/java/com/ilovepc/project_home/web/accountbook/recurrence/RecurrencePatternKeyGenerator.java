package com.ilovepc.project_home.web.accountbook.recurrence;

import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryParam;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 거래 적요와 메모 키워드를 조합해서 반복 거래 후보를 묶을 patternKey를 생성하는 역할을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class RecurrencePatternKeyGenerator {
    private static final String UNKNOWN_DESCRIPTION = "UNKNOWN";

    private final AccountBookTextNormalizer textNormalizer;
    private final MemoKeywordExtractor memoKeywordExtractor;

    /**
     * 금액 변동이 있는 급여, 부수입, 할부 거래도 같은 반복 후보로 묶이도록 금액은 제외하고 키를 생성합니다.
     */
    public String generate(String description, String memo) {
        String descriptionToken = textNormalizer.normalizeDescription(description);
        if (!StringUtils.hasText(descriptionToken)) {
            descriptionToken = UNKNOWN_DESCRIPTION;
        }

        String memoKeyword = memoKeywordExtractor.extract(memo);
        if (!StringUtils.hasText(memoKeyword)) {
            return "DESC:" + descriptionToken;
        }

        return "DESC:" + descriptionToken + "|MEMO:" + memoKeyword;
    }

    /**
     * 적요가 바뀌어도 같은 메모 핵심 키워드로 과거 근거를 찾기 위한 느슨한 fallback key를 생성합니다.
     */
    public String generateFallback(String memo) {
        String memoKeyword = memoKeywordExtractor.extract(memo);
        if (!StringUtils.hasText(memoKeyword)) {
            return null;
        }

        return "MEMO:" + memoKeyword;
    }

    /**
     * 업로드 전에 만들어진 거래 파라미터에서 반복 패턴 키를 생성합니다.
     */
    public String generate(TransactionHistoryParam transaction) {
        if (transaction == null) {
            return generate(null, null);
        }

        return generate(transaction.getDescription(), transaction.getMemo());
    }

    /**
     * 업로드 전에 만들어진 거래 파라미터에서 fallback key를 생성합니다.
     */
    public String generateFallback(TransactionHistoryParam transaction) {
        if (transaction == null) {
            return null;
        }

        return generateFallback(transaction.getMemo());
    }

    /**
     * DB에서 조회한 거래 결과에서 반복 패턴 키를 생성합니다.
     */
    public String generate(TransactionHistoryResult transaction) {
        if (transaction == null) {
            return generate(null, null);
        }

        return generate(transaction.getDescription(), transaction.getMemo());
    }

    /**
     * DB에서 조회한 거래 결과에서 fallback key를 생성합니다.
     */
    public String generateFallback(TransactionHistoryResult transaction) {
        if (transaction == null) {
            return null;
        }

        return generateFallback(transaction.getMemo());
    }
}
