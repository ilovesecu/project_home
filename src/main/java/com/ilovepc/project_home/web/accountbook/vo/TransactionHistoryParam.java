package com.ilovepc.project_home.web.accountbook.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class TransactionHistoryParam {
    private LocalDateTime transactionAt;
    private String description;
    private String transactionType;
    private String transactionInstitution;
    private String accountNumber;
    private Long amount;
    private Long balanceAfter;
    private String memo;
    private String sourceFileName;
    private Integer sourceRowNumber;
}
