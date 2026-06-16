package com.ilovepc.project_home.web.accountbook.service;

import com.ilovepc.project_home.repository.TransactionHistoryMapper;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryResult;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistorySearchParam;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistorySearchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionHistoryQueryService {
    private static final int DEFAULT_LIMIT = 200;
    private static final int MAX_LIMIT = 1000;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TransactionHistoryMapper transactionHistoryMapper;

    public void transactionAmount(LocalDate startDate,
                                  LocalDate endDate){


    }

    public TransactionHistorySearchResponse search(
            LocalDate startDate,
            LocalDate endDate,
            String month,
            int limit,
            int offset
    ) {
        DateRange dateRange = resolveDateRange(startDate, endDate, month);
        int normalizedLimit = normalizeLimit(limit);
        int normalizedOffset = Math.max(offset, 0);

        TransactionHistorySearchParam searchParam = TransactionHistorySearchParam.builder()
                .startDateTime(dateRange.startDate().atStartOfDay())
                .endDateTimeExclusive(dateRange.endDate().plusDays(1).atStartOfDay())
                .limit(normalizedLimit)
                .offset(normalizedOffset)
                .build();

        int totalCount = transactionHistoryMapper.countTransactionHistories(searchParam);
        List<TransactionHistoryResult> transactions = transactionHistoryMapper.selectTransactionHistories(searchParam);

        return TransactionHistorySearchResponse.builder()
                .startDate(dateRange.startDate())
                .endDate(dateRange.endDate())
                .limit(normalizedLimit)
                .offset(normalizedOffset)
                .totalCount(totalCount)
                .transactions(transactions)
                .build();
    }

    private DateRange resolveDateRange(LocalDate startDate, LocalDate endDate, String month) {
        if (StringUtils.hasText(month)) {
            YearMonth yearMonth = parseYearMonth(month);
            return new DateRange(yearMonth.atDay(1), yearMonth.atEndOfMonth());
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate와 endDate를 모두 입력하거나 month를 입력해야 합니다.");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate는 endDate보다 이후일 수 없습니다.");
        }

        return new DateRange(startDate, endDate);
    }

    private YearMonth parseYearMonth(String month) {
        try {
            return YearMonth.parse(month, MONTH_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("month 형식이 올바르지 않습니다. yyyy-MM 형식으로 입력해 주세요.", e);
        }
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
