package com.ilovepc.project_home.web.accountbook.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 같은 recurrencePatternKey를 가진 과거 거래들이 어떤 분류값으로 많이 저장됐는지 요약한 결과입니다.
 */
@Getter
@Setter
public class RecurrenceClassificationSummaryResult {
    private String recurrencePatternKey;
    private String cashflowType;
    private Long categoryId;
    private String categoryName;
    private String recurrenceType;
    private String memoOwner;
    private String classificationStatus;
    private Long evidenceCount;
    private LocalDateTime latestTransactionAt;
}
