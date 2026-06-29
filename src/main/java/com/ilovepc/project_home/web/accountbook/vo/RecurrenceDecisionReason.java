package com.ilovepc.project_home.web.accountbook.vo;

/**
 * 반복성 판정 점수에 영향을 준 근거 코드를 표현합니다.
 */
public enum RecurrenceDecisionReason {
    /**
     * 같은 패턴에 사용자가 직접 분류한 MANUAL 근거가 있습니다.
     */
    MANUAL_EVIDENCE_FOUND,

    /**
     * 같은 패턴에 자동 분류된 AUTO 근거만 있습니다.
     */
    AUTO_EVIDENCE_FOUND,

    /**
     * 같은 패턴의 과거 거래가 2건 이상 반복됐습니다.
     */
    REPEATED_PATTERN,

    /**
     * 과거 분류 요약에서 FIXED가 강하게 나타났습니다.
     */
    FIXED_HISTORY_FOUND,

    /**
     * 과거 분류 요약에서 VARIABLE이 더 강하게 나타났습니다.
     */
    VARIABLE_HISTORY_FOUND,

    /**
     * 과거 거래 금액이 모두 같습니다.
     */
    EXACT_AMOUNT,

    /**
     * 과거 거래 금액이 허용 오차 안에서 비슷하게 움직입니다.
     */
    SIMILAR_AMOUNT,

    /**
     * 과거 거래 금액 변동 폭이 큽니다.
     */
    VARIABLE_AMOUNT,

    /**
     * 금액 패턴을 판단하기에는 과거 거래가 부족합니다.
     */
    INSUFFICIENT_AMOUNT_DATA,

    /**
     * 반복성 자체를 판단하기에는 과거 거래가 부족합니다.
     */
    INSUFFICIENT_EVIDENCE
}
