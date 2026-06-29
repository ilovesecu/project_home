package com.ilovepc.project_home.web.accountbook.vo;

/**
 * 같은 반복 패턴 키로 묶인 과거 거래들의 금액 변동 특징을 표현합니다.
 */
public enum AmountProfileType {
    /**
     * 비교할 과거 거래가 2건 미만이라 금액 패턴을 판단하기 어려운 상태입니다.
     */
    INSUFFICIENT_DATA,

    /**
     * 과거 거래 금액이 모두 같은 상태입니다.
     */
    EXACT,

    /**
     * 금액이 조금씩 다르지만 허용 오차 범위 안에서 움직이는 상태입니다.
     */
    SIMILAR_RANGE,

    /**
     * 금액 변동 폭이 커서 금액만으로 반복성을 강하게 보기 어려운 상태입니다.
     */
    VARIABLE
}
