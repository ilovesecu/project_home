package com.ilovepc.project_home.web.accountbook.vo;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class TransactionHistorySearchResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer limit;
    private Integer offset;
    private Integer totalCount;
    private List<TransactionHistoryResult> transactions;
}
