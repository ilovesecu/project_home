package com.ilovepc.project_home.web.accountbook.service;

import com.ilovepc.project_home.repository.TransactionHistoryMapper;
import com.ilovepc.project_home.web.accountbook.parser.TransactionHistoryFileParser;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryParam;
import com.ilovepc.project_home.web.accountbook.vo.TransactionParseError;
import com.ilovepc.project_home.web.accountbook.vo.TransactionSourceType;
import com.ilovepc.project_home.web.accountbook.vo.TransactionUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionHistoryUploadService {
    private static final int BATCH_SIZE = 5000;

    private final TransactionHistoryMapper transactionHistoryMapper;
    private final List<TransactionHistoryFileParser> transactionHistoryFileParsers;

    @Transactional
    public TransactionUploadResponse upload(MultipartFile file, TransactionSourceType sourceType) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 거래내역 파일이 비어 있습니다.");
        }

        String originalFileName = file.getOriginalFilename();
        List<TransactionParseError> errors = new ArrayList<>();
        AtomicInteger failedCount = new AtomicInteger();
        TransactionHistoryFileParser parser = findParser(sourceType, originalFileName);
        List<TransactionHistoryParam> transactions = parse(parser, file, originalFileName, errors, failedCount);

        int insertedCount = insertBatch(transactions);

        return TransactionUploadResponse.builder()
                .fileName(originalFileName)
                .parsedCount(transactions.size())
                .insertedCount(insertedCount)
                .failedCount(failedCount.get())
                .errors(errors)
                .build();
    }

    private TransactionHistoryFileParser findParser(TransactionSourceType sourceType, String originalFileName) {
        return transactionHistoryFileParsers.stream()
                .filter(parser -> parser.supports(sourceType, originalFileName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 거래내역 원천입니다. sourceType=" + sourceType));
    }

    private List<TransactionHistoryParam> parse(
            TransactionHistoryFileParser parser,
            MultipartFile file,
            String originalFileName,
            List<TransactionParseError> errors,
            AtomicInteger failedCount
    ) {
        try {
            return parser.parse(file, originalFileName, errors, failedCount);
        } catch (IOException e) {
            throw new IllegalArgumentException("거래내역 파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    private int insertBatch(List<TransactionHistoryParam> transactions) {
        if (transactions.isEmpty()) {
            return 0;
        }

        int insertedCount = 0;
        for (int start = 0; start < transactions.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, transactions.size());
            insertedCount += transactionHistoryMapper.insertTransactionHistories(transactions.subList(start, end));
        }
        return insertedCount;
    }
}
