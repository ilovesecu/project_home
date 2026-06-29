package com.ilovepc.project_home.web.accountbook.recurrence;

import com.ilovepc.project_home.web.accountbook.vo.AmountProfileType;
import com.ilovepc.project_home.web.accountbook.vo.RecurrenceAmountProfileResult;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 같은 recurrencePatternKey로 조회한 과거 거래들의 금액 특징을 계산하는 역할을 담당합니다.
 */
@Component
public class RecurrenceAmountProfileCalculator {
    private static final int MINIMUM_EVIDENCE_COUNT = 2;
    private static final BigDecimal SIMILAR_RANGE_TOLERANCE_RATE = new BigDecimal("10.00");

    /**
     * 거래 목록 안의 recurrencePatternKey를 사용해서 금액 특징을 계산합니다.
     */
    public RecurrenceAmountProfileResult calculate(List<TransactionHistoryResult> transactions) {
        return calculate(findPatternKey(transactions), transactions);
    }

    /**
     * 전달받은 recurrencePatternKey와 과거 거래 금액 목록을 기준으로 금액 특징을 계산합니다.
     */
    public RecurrenceAmountProfileResult calculate(
            String recurrencePatternKey,
            List<TransactionHistoryResult> transactions
    ) {
        List<Long> amounts = extractAmounts(transactions);
        if (amounts.size() < MINIMUM_EVIDENCE_COUNT) {
            return buildResult(
                    recurrencePatternKey,
                    AmountProfileType.INSUFFICIENT_DATA,
                    amounts,
                    BigDecimal.ZERO
            );
        }

        BigDecimal varianceRate = calculateVarianceRate(amounts);
        AmountProfileType amountProfileType = resolveAmountProfileType(amounts, varianceRate);

        return buildResult(recurrencePatternKey, amountProfileType, amounts, varianceRate);
    }

    /**
     * null 거래나 금액이 없는 거래를 제외하고 금액만 추출합니다.
     */
    private List<Long> extractAmounts(List<TransactionHistoryResult> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return List.of();
        }

        return transactions.stream()
                .filter(Objects::nonNull)
                .map(TransactionHistoryResult::getAmount)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * 명시적인 patternKey가 없을 때 거래 목록에서 첫 번째 patternKey를 찾습니다.
     */
    private String findPatternKey(List<TransactionHistoryResult> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return null;
        }

        return transactions.stream()
                .filter(Objects::nonNull)
                .map(TransactionHistoryResult::getRecurrencePatternKey)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    /**
     * 최저~최고 금액 차이가 평균 절대값 대비 몇 퍼센트인지 계산합니다.
     */
    private BigDecimal calculateVarianceRate(List<Long> amounts) {
        long amountMin = min(amounts);
        long amountMax = max(amounts);
        long amountAverage = average(amounts);
        long amountSpread = amountMax - amountMin;

        if (amountSpread == 0L) {
            return BigDecimal.ZERO;
        }

        BigDecimal averageAbs = BigDecimal.valueOf(amountAverage).abs();
        if (BigDecimal.ZERO.compareTo(averageAbs) == 0) {
            return SIMILAR_RANGE_TOLERANCE_RATE.add(BigDecimal.ONE);
        }

        return BigDecimal.valueOf(amountSpread)
                .divide(averageAbs, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 금액 목록과 변동률을 기준으로 amount profile 타입을 결정합니다.
     */
    private AmountProfileType resolveAmountProfileType(List<Long> amounts, BigDecimal varianceRate) {
        if (allSameAmount(amounts)) {
            return AmountProfileType.EXACT;
        }

        if (varianceRate.compareTo(SIMILAR_RANGE_TOLERANCE_RATE) <= 0) {
            return AmountProfileType.SIMILAR_RANGE;
        }

        return AmountProfileType.VARIABLE;
    }

    /**
     * 계산된 금액 통계와 profile 타입을 결과 객체로 묶습니다.
     */
    private RecurrenceAmountProfileResult buildResult(
            String recurrencePatternKey,
            AmountProfileType amountProfileType,
            List<Long> amounts,
            BigDecimal varianceRate
    ) {
        return RecurrenceAmountProfileResult.builder()
                .recurrencePatternKey(recurrencePatternKey)
                .amountProfileType(amountProfileType)
                .occurrenceCount(amounts.size())
                .amountMin(amounts.isEmpty() ? null : min(amounts))
                .amountMax(amounts.isEmpty() ? null : max(amounts))
                .amountAverage(amounts.isEmpty() ? null : average(amounts))
                .amountSpread(amounts.isEmpty() ? null : max(amounts) - min(amounts))
                .amountVarianceRate(varianceRate)
                .similarRangeToleranceRate(SIMILAR_RANGE_TOLERANCE_RATE)
                .build();
    }

    /**
     * 모든 과거 거래가 같은 금액인지 확인합니다.
     */
    private boolean allSameAmount(List<Long> amounts) {
        long firstAmount = amounts.get(0);
        return amounts.stream().allMatch(amount -> amount == firstAmount);
    }

    private long min(List<Long> amounts) {
        return amounts.stream().min(Comparator.naturalOrder()).orElse(0L);
    }

    private long max(List<Long> amounts) {
        return amounts.stream().max(Comparator.naturalOrder()).orElse(0L);
    }

    private long average(List<Long> amounts) {
        return Math.round(amounts.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0D));
    }
}
