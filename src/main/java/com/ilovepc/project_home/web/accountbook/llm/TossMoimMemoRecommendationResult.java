package com.ilovepc.project_home.web.accountbook.llm;

public record TossMoimMemoRecommendationResult(
        Integer sourceRowNumber,
        String recommendedMemo,
        String cashflowType,
        String recurrenceType,
        String categoryName,
        String memoOwner,
        Integer confidence,
        String reason
) {
}
