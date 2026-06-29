package com.ilovepc.project_home.web.accountbook.llm;

public record OnnuriMemoClassificationResult(
        String cashflowType,
        String recurrenceType,
        String categoryName,
        String memoOwner,
        String recommendedMemo,
        Integer confidence,
        String reason,
        String businessType
) {
}
