package com.ilovepc.project_home.web.accountbook.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TransactionHistorySearchParam {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTimeExclusive;
    private Integer limit;
    private Integer offset;
}
