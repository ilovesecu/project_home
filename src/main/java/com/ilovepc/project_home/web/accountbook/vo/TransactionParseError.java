package com.ilovepc.project_home.web.accountbook.vo;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransactionParseError {
    private Integer rowNumber;
    private String message;
}
