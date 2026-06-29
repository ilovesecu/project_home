package com.ilovepc.project_home.web.accountbook.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 과거 분류 근거와 금액 특징을 점수화해서 추천 반복유형을 계산한 결과입니다.
 */
@Getter
@Builder
public class RecurrenceDecisionResult {
    private String recurrencePatternKey;
    private String recommendedRecurrenceType;
    private Integer confidence;
    private Integer score;
    private Integer evidenceCount;
    private Integer manualEvidenceCount;
    private Integer autoEvidenceCount;
    private AmountProfileType amountProfileType;
    private List<RecurrenceDecisionReason> reasonCodes;
}
