package com.ilovepc.project_home.web.accountbook.llm;

import java.util.List;

public record OnnuriMemoClassificationRequest(
        String merchantName,
        Long amount,
        String transactionType,
        String transactionStatus,
        String businessRegistrationNumber,
        List<String> allowedOwners
) {
}
