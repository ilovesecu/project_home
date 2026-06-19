package com.ilovepc.project_home.web.accountbook.parser;

import com.ilovepc.project_home.web.accountbook.vo.TransactionParseError;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

public abstract class AbstractTransactionHistoryFileParser implements TransactionHistoryFileParser {
    protected boolean isBlankRow(String[] row) {
        if (row == null || row.length == 0) {
            return true;
        }
        for (String value : row) {
            if (StringUtils.hasText(value)) {
                return false;
            }
        }
        return true;
    }

    protected String value(String[] row, int index) {
        if (row == null || index >= row.length || row[index] == null) {
            return "";
        }
        return row[index].trim();
    }

    protected Long parseAmount(String value, String columnName) {
        String normalized = value.replace(",", "").replace("\u2212", "-").trim();
        if (!StringUtils.hasText(normalized)) {
            throw new IllegalArgumentException(columnName + " 값이 비어 있습니다.");
        }
        try {
            return Long.parseLong(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(columnName + " 형식이 올바르지 않습니다. value=" + value, e);
        }
    }

    protected void addError(List<TransactionParseError> errors, int rowNumber, String message) {
        if (errors.size() < 50) {
            errors.add(TransactionParseError.builder()
                    .rowNumber(rowNumber)
                    .message(message)
                    .build());
        }
    }

    protected String sha256Key(String... values) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String joined = String.join("|", values);
            return HexFormat.of().formatHex(digest.digest(joined.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    protected String getExtension(String originalFileName) {
        if (!StringUtils.hasText(originalFileName) || !originalFileName.contains(".")) {
            return "";
        }
        return originalFileName.substring(originalFileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
