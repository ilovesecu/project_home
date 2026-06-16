package com.ilovepc.project_home.web.accountbook.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionHistoryResult {
    private Long id;
    private LocalDateTime transactionAt;
    private String description;
    private String transactionType;
    private String transactionInstitution;
    private String accountNumber;
    private Long amount;
    private Long balanceAfter;
    private String memo;
}
