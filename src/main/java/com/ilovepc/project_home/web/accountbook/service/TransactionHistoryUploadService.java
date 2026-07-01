package com.ilovepc.project_home.web.accountbook.service;

import com.ilovepc.project_home.repository.TransactionHistoryMapper;
import com.ilovepc.project_home.web.accountbook.classification.TransactionMemoClassificationResult;
import com.ilovepc.project_home.web.accountbook.llm.TossMoimMemoMakerService;
import com.ilovepc.project_home.web.accountbook.llm.TossMoimMemoRecommendationResult;
import com.ilovepc.project_home.web.accountbook.parser.TransactionHistoryFileParser;
import com.ilovepc.project_home.web.accountbook.recurrence.RecurrenceAmountProfileCalculator;
import com.ilovepc.project_home.web.accountbook.recurrence.RecurrenceDecisionService;
import com.ilovepc.project_home.web.accountbook.recurrence.RecurrencePatternKeyGenerator;
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
    private static final int MAKE_MEMO_EVIDENCE_LIMIT = 20;
    private static final int MAKE_MEMO_LLM_TARGET_LIMIT = 10;
    private final TossMoimMemoMakerService tossMoimMemoMakerService;
    private final TransactionHistoryMapper transactionHistoryMapper;
    private final List<TransactionHistoryFileParser> transactionHistoryFileParsers;
    private final TransactionMemoClassificationService transactionMemoClassificationService;
    private final RecurrencePatternKeyGenerator recurrencePatternKeyGenerator;
    private final RecurrenceAmountProfileCalculator recurrenceAmountProfileCalculator;
    private final RecurrenceDecisionService recurrenceDecisionService;

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

        List<TransactionHistoryParam> recurrencePatternKeyAppliedTransactions = applyRecurrencePatternKeys(
                classificationResult.transactions()
        );
        int insertedCount = insertBatch(recurrencePatternKeyAppliedTransactions);

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
            throw new IllegalArgumentException("메모를 만들 거래내역 파일이 비어 있습니다.");
        }
        String originalFileName = file.getOriginalFilename();
        List<TransactionParseError> errors = new ArrayList<>();
        AtomicInteger failedCount = new AtomicInteger();
        TransactionHistoryFileParser parser = findParser(sourceType, originalFileName);
        List<TransactionHistoryParam> transactions = parse(parser, file, originalFileName, errors, failedCount);
        List<TransactionHistoryParam> recurrencePatternKeyAppliedTransactions = applyRecurrencePatternKeys(transactions);
        List<MakeMemoDecisionPreviewResult> decisionPreviews = buildMakeMemoDecisionPreviews(
                recurrencePatternKeyAppliedTransactions
        );
        List<TransactionHistoryResult> historicalExamples = transactionHistoryMapper.selectExample10();
        List<AccountCategoryResult> accountCategoryResults = transactionHistoryMapper.selectMakeMemoCategories();
        List<TossMoimMemoRecommendationResult> recommendations = tossMoimMemoMakerService.tossMoimMemoMaker(
                historicalExamples,
                accountCategoryResults,
                limitMakeMemoLlmTargets(decisionPreviews)
        );

        log.info(
                "MAKE MEMO DECISION PREVIEW CREATED : fileName={}, parsedCount={}, previewCount={}",
                originalFileName,
                transactions.size(),
                decisionPreviews.size()
        );

        return TransactionUploadResponse.builder()
                .fileName(originalFileName)
                .parsedCount(transactions.size())
                .insertedCount(0)
                .failedCount(failedCount.get())
                .warningCount(0)
                .errors(errors)
                .warnings(List.of())
                .makeMemoDecisionPreviews(decisionPreviews) //백엔드가 계산한 고정/변동 판단 근거 (지우지마)
                .makeMemoRecommendations(recommendations) //Gemini가 추천한 최종 메모 (지우지마)
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

    /**
     * 업로드 저장 직전에 반복 패턴 키를 생성합니다.
     * 메모 분류 결과가 반영된 뒤 생성해야 category/memo 기반 반복 후보 분석과 같은 기준을 사용할 수 있습니다.
     */
    private List<TransactionHistoryParam> applyRecurrencePatternKeys(List<TransactionHistoryParam> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return List.of();
        }

        return transactions.stream()
                .map(transaction -> transaction.toBuilder()
                        .recurrencePatternKey(recurrencePatternKeyGenerator.generate(transaction))
                        .recurrenceFallbackKey(recurrencePatternKeyGenerator.generateFallback(transaction))
                        .build())
                .toList();
    }

    /**
     * makeMemo 대상 거래마다 과거 근거, 금액 특징, 반복성 decision을 계산해 응답에서 확인할 수 있게 만듭니다.
     */
    private List<MakeMemoDecisionPreviewResult> buildMakeMemoDecisionPreviews(List<TransactionHistoryParam> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return List.of();
        }

        return transactions.stream()
                .map(this::buildMakeMemoDecisionPreview)
                .toList();
    }

    /**
     * Gemini timeout 원인 확인을 위해 LLM에 보내는 대상 거래 수만 임시로 제한합니다.
     * decision preview 응답은 전체를 유지합니다.
     */
    private List<MakeMemoDecisionPreviewResult> limitMakeMemoLlmTargets(
            List<MakeMemoDecisionPreviewResult> decisionPreviews
    ) {
        if (decisionPreviews == null || decisionPreviews.isEmpty()) {
            return List.of();
        }

        return decisionPreviews.stream()
                .limit(MAKE_MEMO_LLM_TARGET_LIMIT)
                .toList();
    }

    /**
     * 거래 1건의 recurrencePatternKey를 기준으로 2~4단계 근거를 계산합니다.
     */
    private MakeMemoDecisionPreviewResult buildMakeMemoDecisionPreview(TransactionHistoryParam transaction) {
        String recurrencePatternKey = transaction.getRecurrencePatternKey();
        String recurrenceFallbackKey = transaction.getRecurrenceFallbackKey();
        List<TransactionHistoryResult> evidenceTransactions =
                transactionHistoryMapper.selectClassificationEvidenceByPatternKeys(
                        recurrencePatternKey,
                        recurrenceFallbackKey,
                        MAKE_MEMO_EVIDENCE_LIMIT
                );
        List<RecurrenceClassificationSummaryResult> classificationSummaries =
                transactionHistoryMapper.selectClassificationSummaryByPatternKeys(
                        recurrencePatternKey,
                        recurrenceFallbackKey
                );
        RecurrenceAmountProfileResult amountProfile = recurrenceAmountProfileCalculator.calculate(
                recurrencePatternKey,
                evidenceTransactions
        );
        RecurrenceDecisionResult decision = recurrenceDecisionService.decide(
                recurrencePatternKey,
                evidenceTransactions,
                classificationSummaries,
                amountProfile
        );

        return MakeMemoDecisionPreviewResult.builder()
                .sourceRowNumber(transaction.getSourceRowNumber())
                .transactionAt(transaction.getTransactionAt())
                .description(transaction.getDescription())
                .amount(transaction.getAmount())
                .memo(transaction.getMemo())
                .recurrencePatternKey(recurrencePatternKey)
                .recurrenceFallbackKey(recurrenceFallbackKey)
                .amountProfile(amountProfile)
                .decision(decision)
                .build();
    }
}
