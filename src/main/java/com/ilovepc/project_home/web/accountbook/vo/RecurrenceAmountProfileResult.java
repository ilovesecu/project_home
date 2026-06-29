package com.ilovepc.project_home.web.accountbook.vo;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * 같은 recurrencePatternKey 거래 묶음의 금액 통계를 계산한 결과입니다.
 */
@Getter
@Builder
public class RecurrenceAmountProfileResult {
    private String recurrencePatternKey;
    private AmountProfileType amountProfileType;
    private Integer occurrenceCount;
    private Long amountMin;
    private Long amountMax;
    private Long amountAverage;
    private Long amountSpread;
    private BigDecimal amountVarianceRate;
    private BigDecimal similarRangeToleranceRate;
}
