package com.ilovepc.project_home.web.accountbook.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * makeMemo 실행 전에 대상 거래별 반복성 판정 결과를 확인하기 위한 미리보기 응답입니다.
 */
@Getter
@Builder
public class MakeMemoDecisionPreviewResult {
    private Integer sourceRowNumber;
    private LocalDateTime transactionAt;
    private String description;
    private Long amount;
    private String memo;
    private String recurrencePatternKey;
    private String recurrenceFallbackKey;
    private RecurrenceAmountProfileResult amountProfile;
    private RecurrenceDecisionResult decision;
}
