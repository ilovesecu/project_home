package com.ilovepc.project_home.web.accountbook.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionHistoryAmountParam {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTimeExclusive;
}
