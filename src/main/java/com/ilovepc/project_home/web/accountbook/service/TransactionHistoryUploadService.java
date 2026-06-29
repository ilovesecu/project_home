package com.ilovepc.project_home.web.accountbook.service;

import com.ilovepc.project_home.repository.TransactionHistoryMapper;
import com.ilovepc.project_home.web.accountbook.classification.TransactionMemoClassificationResult;
import com.ilovepc.project_home.web.accountbook.llm.TossMoimMemoMakerService;
import com.ilovepc.project_home.web.accountbook.parser.TransactionHistoryFileParser;
import com.ilovepc.project_home.web.accountbook.vo.*;
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
    private final TossMoimMemoMakerService tossMoimMemoMakerService;
    private final TransactionHistoryMapper transactionHistoryMapper;
    private final List<TransactionHistoryFileParser> transactionHistoryFileParsers;
    private final TransactionMemoClassificationService transactionMemoClassificationService;

    @Transactional
    public TransactionUploadResponse upload(MultipartFile file, TransactionSourceType sourceType) {
        /*
        [파일 업로드]
        → Toss/Onnuri 거래내역 파싱
        → 규칙 시트 읽기
        → 메모 파싱
        → 카테고리 DB upsert
        → 거래별 category_id 매핑
        → transaction_history batch insert
        */

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 거래내역 파일이 비어 있습니다.");
        }

        String originalFileName = file.getOriginalFilename();
        List<TransactionParseError> errors = new ArrayList<>();
        List<TransactionParseError> warnings = new ArrayList<>();
        AtomicInteger failedCount = new AtomicInteger();
        TransactionHistoryFileParser parser = findParser(sourceType, originalFileName);
        List<TransactionHistoryParam> transactions = parse(parser, file, originalFileName, errors, failedCount);
        TransactionMemoClassificationResult classificationResult = transactionMemoClassificationService.classify(
                file,
                originalFileName,
                transactions,
                warnings
        );

        int insertedCount = insertBatch(classificationResult.transactions());

        return TransactionUploadResponse.builder()
                .fileName(originalFileName)
                .parsedCount(transactions.size())
                .insertedCount(insertedCount)
                .failedCount(failedCount.get())
                .warningCount(classificationResult.warningCount())
                .errors(errors)
                .warnings(warnings)
                .build();
    }

    public TransactionUploadResponse makeMemo(MultipartFile file, TransactionSourceType sourceType){
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("메모를 파싱할 거래내역 파일이 비어 있습니다.");
        }
        String originalFileName = file.getOriginalFilename();
        List<TransactionParseError> errors = new ArrayList<>();
        List<TransactionParseError> warnings = new ArrayList<>();
        AtomicInteger failedCount = new AtomicInteger();
        TransactionHistoryFileParser parser = findParser(sourceType, originalFileName);
        try{
            String[] strings = parser.parseMemo(file);
            List<TransactionHistoryResult> transactionHistoryResults = transactionHistoryMapper.selectExample10();
            //카테고리 가져오기
            List<AccountCategoryResult> accountCategoryResults = transactionHistoryMapper.selectMakeMemoCategories();
            tossMoimMemoMakerService.tossMoimMemoMaker(transactionHistoryResults,accountCategoryResults);
        }catch (IOException ioException){
            log.error("makeMemo IOException", ioException);
        }

        //List<TransactionHistoryParam> transactions = parse(parser, file, originalFileName, errors, failedCount);

        return null;
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
