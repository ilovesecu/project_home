package com.ilovepc.project_home.web.accountbook.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TransactionUploadResponse {
    private String fileName;
    private Integer parsedCount;
    private Integer insertedCount;
    private Integer failedCount;
    private List<TransactionParseError> errors;
}
