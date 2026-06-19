package com.ilovepc.project_home.web.accountbook.vo;

import java.util.Locale;

public enum TransactionSourceType {
    TOSS_BANK,
    ONNURI_GIFT_CARD;

    public static TransactionSourceType from(String value) {
        if (value == null || value.isBlank()) {
            return TOSS_BANK;
        }
        return TransactionSourceType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
