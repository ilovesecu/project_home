package com.ilovepc.project_home.web.accountbook.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.config.llm.AccountBookLlmProperties;
import com.ilovepc.project_home.web.accountbook.recurrence.RecurrencePatternKeyGenerator;
import com.ilovepc.project_home.web.accountbook.vo.AccountCategoryResult;
import com.ilovepc.project_home.web.accountbook.vo.MakeMemoDecisionPreviewResult;
import com.ilovepc.project_home.web.accountbook.vo.RecurrenceAmountProfileResult;
import com.ilovepc.project_home.web.accountbook.vo.RecurrenceDecisionResult;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 토스 모임통장 거래의 과거 분류 예시와 허용 카테고리를 Gemini 입력값으로 구성하는 역할을 담당합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TossMoimMemoMakerService {
    private static final List<String> ALLOWED_OWNERS = List.of("@공동", "@승주", "@성은");
    private static final List<String> ALLOWED_RECURRENCE_TYPES = List.of("FIXED", "VARIABLE", "ONE_TIME");

    private final GeminiInteractionsClient geminiInteractionsClient;
    private final AccountBookLlmProperties accountBookLlmProperties;
    private final ObjectMapper objectMapper;
    private final RecurrencePatternKeyGenerator recurrencePatternKeyGenerator;

    /**
     * makeMemo 분류 요청에 필요한 입력값과 응답 스키마를 준비하는 서비스 진입점입니다.
     */
    public void tossMoimMemoMaker(
            List<TransactionHistoryResult> example10,
            List<AccountCategoryResult> accountCategoryResults
    ) {
        tossMoimMemoMaker(example10, accountCategoryResults, List.of());
    }

    /**
     * makeMemo 대상 거래와 백엔드 반복성 판단 근거까지 포함해 Gemini 입력값을 준비합니다.
     */
    public void tossMoimMemoMaker(
            List<TransactionHistoryResult> example10,
            List<AccountCategoryResult> accountCategoryResults,
            List<MakeMemoDecisionPreviewResult> targetTransactions
    ) {
        Map<String, Object> responseSchema = responseSchemaTossMoimMemo();
        String geminiInput = buildGeminiInput(example10, accountCategoryResults, targetTransactions);

        log.info(
                "TOSS MOIM MEMO MAKER INPUT READY : provider={}, exampleCount={}, categoryCount={}, targetCount={}, schemaKeys={}",
                accountBookLlmProperties.getProvider(),
                example10 == null ? 0 : example10.size(),
                accountCategoryResults == null ? 0 : accountCategoryResults.size(),
                targetTransactions == null ? 0 : targetTransactions.size(),
                responseSchema.keySet()
        );
        log.debug("TOSS MOIM MEMO MAKER GEMINI INPUT={}", geminiInput);

        // TODO: Wire target transactions and parse Gemini response when makeMemo result DTO is decided.
        // String outputText = geminiInteractionsClient.createStructuredResponse(geminiInput, responseSchema);
    }

    /**
     * Gemini가 따라야 할 시스템 지시문과 실제 거래/카테고리 payload를 하나의 프롬프트 문자열로 합칩니다.
     */
    private String buildGeminiInput(
            List<TransactionHistoryResult> examples,
            List<AccountCategoryResult> categoryResults,
            List<MakeMemoDecisionPreviewResult> targetTransactions
    ) {
        return systemInstructionTossMoimMemoClassify()
                + "\n\n반드시 응답 스키마에 맞는 JSON만 반환한다."
                + "\n\n입력 데이터:\n"
                + buildClassificationPayload(examples, categoryResults, targetTransactions);
    }

    /**
     * 허용 카테고리, 과거 예시, 메모 규칙처럼 모델 판단에 필요한 구조화 데이터를 만듭니다.
     */
    private String buildClassificationPayload(
            List<TransactionHistoryResult> examples,
            List<AccountCategoryResult> categoryResults,
            List<MakeMemoDecisionPreviewResult> targetTransactions
    ) {
        Map<String, Object> payload = Map.of(
                "purpose", "토스 모임통장 거래에 사용할 추천 메모를 생성한다.",
                "memoRule", "[대분류][고정여부][카테고리] @주체 내용",
                "allowedOwners", ALLOWED_OWNERS,
                "allowedRecurrenceTypes", ALLOWED_RECURRENCE_TYPES,
                "allowedCategories", groupCategoriesByCashflowType(categoryResults),
                "historicalExamples", normalizeExamples(examples),
                "targetTransactions", normalizeTargets(targetTransactions)
        );

        return toJson(payload);
    }

    /**
     * DB 카테고리 목록을 cashflowType별로 묶어 Gemini가 선택 가능한 카테고리 사전으로 변환합니다.
     */
    private Map<String, List<String>> groupCategoriesByCashflowType(List<AccountCategoryResult> categoryResults) {
        if (categoryResults == null || categoryResults.isEmpty()) {
            return Map.of();
        }

        return categoryResults.stream()
                .filter(Objects::nonNull)
                .filter(category -> category.getCashflowType() != null && category.getName() != null)
                .collect(Collectors.groupingBy(
                        AccountCategoryResult::getCashflowType,
                        Collectors.mapping(
                                AccountCategoryResult::getName,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        names -> names.stream().distinct().toList()
                                )
                        )
                ));
    }

    /**
     * 과거 거래 예시를 LLM 입력에 필요한 최소 필드와 반복 패턴 키만 남긴 형태로 정리합니다.
     */
    private List<Map<String, Object>> normalizeExamples(List<TransactionHistoryResult> examples) {
        if (examples == null || examples.isEmpty()) {
            return List.of();
        }

        return examples.stream()
                .filter(Objects::nonNull)
                .map(example -> Map.<String, Object>of(
                        "transactionAt", nullToEmpty(example.getTransactionAt() == null
                                ? null
                                : example.getTransactionAt().toString()),
                        "merchantName", nullToEmpty(example.getDescription()),
                        "amount", example.getAmount() == null ? 0 : example.getAmount(),
                        "memo", nullToEmpty(example.getMemo()),
                        "recurrencePatternKey", recurrencePatternKeyGenerator.generate(example),
                        "cashflowType", nullToEmpty(example.getCashflowType()),
                        "categoryId", example.getCategoryId() == null ? 0 : example.getCategoryId(),
                        "recurrenceType", nullToEmpty(example.getRecurrenceType()),
                        "memoOwner", nullToEmpty(example.getMemoOwner()),
                        "classificationStatus", nullToEmpty(example.getClassificationStatus())
                ))
                .toList();
    }

    /**
     * makeMemo 대상 거래를 Gemini가 판단하기 쉬운 구조로 정리합니다.
     */
    private List<Map<String, Object>> normalizeTargets(List<MakeMemoDecisionPreviewResult> targetTransactions) {
        if (targetTransactions == null || targetTransactions.isEmpty()) {
            return List.of();
        }

        return targetTransactions.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeTarget)
                .toList();
    }

    /**
     * 대상 거래 1건에 백엔드 decision과 amount profile을 함께 담습니다.
     */
    private Map<String, Object> normalizeTarget(MakeMemoDecisionPreviewResult target) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sourceRowNumber", target.getSourceRowNumber());
        payload.put("transactionAt", target.getTransactionAt() == null ? "" : target.getTransactionAt().toString());
        payload.put("merchantName", nullToEmpty(target.getDescription()));
        payload.put("amount", target.getAmount() == null ? 0 : target.getAmount());
        payload.put("memo", nullToEmpty(target.getMemo()));
        payload.put("recurrencePatternKey", nullToEmpty(target.getRecurrencePatternKey()));
        payload.put("amountProfile", normalizeAmountProfile(target.getAmountProfile()));
        payload.put("backendDecision", normalizeDecision(target.getDecision()));
        return payload;
    }

    /**
     * amount profile 결과를 LLM 입력용 JSON 객체로 변환합니다.
     */
    private Map<String, Object> normalizeAmountProfile(RecurrenceAmountProfileResult amountProfile) {
        if (amountProfile == null) {
            return Map.of();
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("amountProfileType", amountProfile.getAmountProfileType());
        payload.put("occurrenceCount", amountProfile.getOccurrenceCount());
        payload.put("amountMin", amountProfile.getAmountMin());
        payload.put("amountMax", amountProfile.getAmountMax());
        payload.put("amountAverage", amountProfile.getAmountAverage());
        payload.put("amountSpread", amountProfile.getAmountSpread());
        payload.put("amountVarianceRate", amountProfile.getAmountVarianceRate());
        payload.put("similarRangeToleranceRate", amountProfile.getSimilarRangeToleranceRate());
        return payload;
    }

    /**
     * 백엔드 반복성 판정 결과를 LLM 입력용 JSON 객체로 변환합니다.
     */
    private Map<String, Object> normalizeDecision(RecurrenceDecisionResult decision) {
        if (decision == null) {
            return Map.of();
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("recommendedRecurrenceType", nullToEmpty(decision.getRecommendedRecurrenceType()));
        payload.put("confidence", decision.getConfidence());
        payload.put("score", decision.getScore());
        payload.put("evidenceCount", decision.getEvidenceCount());
        payload.put("manualEvidenceCount", decision.getManualEvidenceCount());
        payload.put("autoEvidenceCount", decision.getAutoEvidenceCount());
        payload.put("amountProfileType", decision.getAmountProfileType());
        payload.put("reasonCodes", decision.getReasonCodes() == null ? List.of() : decision.getReasonCodes());
        return payload;
    }

    /**
     * Gemini가 추천 메모를 만들 때 지켜야 할 분류 원칙과 출력 규칙을 정의합니다.
     */
    private String systemInstructionTossMoimMemoClassify() {
        return "너는 신혼부부 가계부의 토스 모임통장 거래 메모를 추천하는 분류기다. "
                + "과거 거래 예시는 판단 근거로만 사용하고, 반드시 입력 payload에 제공된 허용값 안에서만 선택한다. "
                + "classificationStatus가 MANUAL인 과거 예시는 AUTO 예시보다 강한 근거로 본다. "
                + "targetTransactions의 backendDecision과 amountProfile은 서버가 계산한 근거이므로 추천 메모를 만들 때 우선 참고한다. "
                + "recommendedMemo는 반드시 [대분류][고정여부][카테고리] @주체 내용 형식으로 만든다. "
                + "대분류와 카테고리는 allowedCategories에 있는 값만 사용한다. "
                + "주체는 @공동, @승주, @성은 중 하나만 사용한다. "
                + "고정여부는 반복 거래면 [고정], 일반 소비면 [변동], 일회성 거래면 [일회성]으로 판단한다. "
                + "근거가 약하면 가능한 경우 카테고리는 기타로 선택하고 confidence를 낮게 준다. "
                + "계좌번호, 카드번호, 잔액처럼 입력에 없는 정보는 만들지 않는다. "
                + "반드시 JSON만 반환한다.";
    }

    /**
     * Gemini 응답이 항상 makeMemo 결과 배열 형태로 오도록 강제하는 JSON 스키마를 정의합니다.
     */
    private Map<String, Object> responseSchemaTossMoimMemo() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of("items"),
                "properties", Map.of(
                        "items", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "additionalProperties", false,
                                        "required", List.of(
                                                "recommendedMemo",
                                                "cashflowType",
                                                "recurrenceType",
                                                "categoryName",
                                                "memoOwner",
                                                "confidence",
                                                "reason"
                                        ),
                                        "properties", Map.of(
                                                "recommendedMemo", Map.of("type", "string"),
                                                "cashflowType", Map.of("type", "string"),
                                                "recurrenceType", Map.of("type", "string"),
                                                "categoryName", Map.of("type", "string"),
                                                "memoOwner", Map.of("type", "string", "enum", ALLOWED_OWNERS),
                                                "confidence", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                                                "reason", Map.of("type", "string")
                                        )
                                )
                        )
                )
        );
    }

    /**
     * null 문자열이 LLM 입력에 그대로 들어가지 않도록 빈 문자열로 치환합니다.
     */
    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * Gemini 입력 payload를 JSON 문자열로 변환하고, 변환 실패 시 호출부가 알 수 있는 예외로 감쌉니다.
     */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to create Toss moim memo maker JSON.", e);
        }
    }
}
