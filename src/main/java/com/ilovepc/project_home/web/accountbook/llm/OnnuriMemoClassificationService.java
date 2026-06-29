package com.ilovepc.project_home.web.accountbook.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.config.llm.AccountBookLlmProperties;
import com.ilovepc.project_home.config.openai.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OnnuriMemoClassificationService {
    private static final String PROVIDER_GEMINI = "gemini";
    private static final String PROVIDER_OPENAI = "openai";
    private static final List<String> DEFAULT_CATEGORIES = List.of(
            "식비", "생활", "카페", "의료", "교통", "차량", "문화", "기타", "의류"
    );
    private static final List<String> DEFAULT_OWNERS = List.of("@공동", "@승주", "@성은");

    private final OpenAiResponsesClient openAiResponsesClient;
    private final GeminiInteractionsClient geminiInteractionsClient;
    private final OpenAiProperties openAiProperties;
    private final AccountBookLlmProperties accountBookLlmProperties;
    private final ObjectMapper objectMapper;

    public OnnuriMemoClassificationResult classify(OnnuriMemoClassificationRequest request) {
        List<String> allowedOwners = normalizeList(request.allowedOwners(), DEFAULT_OWNERS);
        Map<String, Object> responseSchema = responseSchema(DEFAULT_CATEGORIES, allowedOwners);

        String outputText = createStructuredOutput(request, DEFAULT_CATEGORIES, allowedOwners, responseSchema);
        try {
            OnnuriMemoClassificationResult result = objectMapper.readValue(
                    outputText,
                    OnnuriMemoClassificationResult.class
            );
            validateResult(result, DEFAULT_CATEGORIES, allowedOwners);
            return result;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("LLM classification response is not valid JSON. response=" + outputText, e);
        }
    }

    private String createStructuredOutput(
            OnnuriMemoClassificationRequest request,
            List<String> allowedCategories,
            List<String> allowedOwners,
            Map<String, Object> responseSchema
    ) {
        String provider = StringUtils.hasText(accountBookLlmProperties.getProvider())
                ? accountBookLlmProperties.getProvider().trim().toLowerCase()
                : PROVIDER_GEMINI;

        if (PROVIDER_GEMINI.equals(provider)) {
            return geminiInteractionsClient.createStructuredResponse(
                    buildGeminiInput(request, allowedCategories, allowedOwners),
                    responseSchema
            );
        }
        if (PROVIDER_OPENAI.equals(provider)) {
            return openAiResponsesClient.createStructuredResponse(
                    buildOpenAiRequestBody(request, allowedCategories, allowedOwners, responseSchema)
            );
        }

        throw new IllegalStateException("Unsupported account-book.llm.provider=" + provider);
    }

    private Map<String, Object> buildOpenAiRequestBody(
            OnnuriMemoClassificationRequest request,
            List<String> allowedCategories,
            List<String> allowedOwners,
            Map<String, Object> responseSchema
    ) {
        return Map.of(
                "model", openAiProperties.getModel(),
                "input", List.of(
                        Map.of(
                                "role", "system",
                                "content", systemInstruction()
                        ),
                        Map.of(
                                "role", "user",
                                "content", buildClassificationPayload(request, allowedCategories, allowedOwners)
                        )
                ),
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "onnuri_memo_classification",
                                "strict", true,
                                "schema", responseSchema
                        )
                ),
                "temperature", 0.2,
                "max_output_tokens", 400,
                "store", false
        );
    }

    private String buildGeminiInput(
            OnnuriMemoClassificationRequest request,
            List<String> allowedCategories,
            List<String> allowedOwners
    ) {
        return systemInstruction()
                + "\n\nReturn only JSON that matches the schema."
                + "\n\nInput:\n"
                + buildClassificationPayload(request, allowedCategories, allowedOwners);
    }

    private String buildClassificationPayload(
            OnnuriMemoClassificationRequest request,
            List<String> allowedCategories,
            List<String> allowedOwners
    ) {
        Map<String, Object> transaction = Map.of(
                "sourceType", "ONNURI_GIFT_CARD",
                "merchantName", nullToEmpty(request.merchantName()),
                "amount", request.amount() == null ? 0 : request.amount(),
                "transactionType", nullToEmpty(request.transactionType()),
                "transactionStatus", nullToEmpty(request.transactionStatus()),
                "businessRegistrationNumber", request.businessRegistrationNumber()
        );

        Map<String, Object> payload = Map.of(
                "transaction", transaction,
                "allowedCashflowTypes", List.of("EXPENSE", "ETC"),
                "allowedRecurrenceTypes", List.of("VARIABLE", "ONE_TIME"),
                "allowedCategories", allowedCategories,
                "allowedOwners", allowedOwners,
                "memoRule", "[대분류][고정여부][카테고리] @주체 내용"
        );

        return toJson(payload);
    }

    private String systemInstruction() {
        return "너는 신혼부부 가계부의 온누리상품권 거래 메모를 분류한다. "
                + "반드시 허용된 값 중에서만 고르고, 애매하면 categoryName은 기타로 선택한다. "
                + "사업자 번호인 businessRegistrationNumber로 웹에서 검색(moneypin.biz/bizno/, marketbz.com, bizno.net, naver.com 등) 후 어떤 기업인지 파악 후 카테고리를 만들어주고 어떤 비즈니스 타입인지 결과로 알려줘"
                + "계좌번호, 잔액, 카드번호처럼 입력되지 않은 정보는 만들지 않는다. "
                + "recommendedMemo는 예: [지출][변동][식비] @공동 온누리상품권 식료품 형식으로 만든다.";
    }

    private Map<String, Object> responseSchema(List<String> allowedCategories, List<String> allowedOwners) {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "required", List.of(
                        "cashflowType",
                        "recurrenceType",
                        "categoryName",
                        "memoOwner",
                        "recommendedMemo",
                        "confidence",
                        "reason",
                        "BusinessType"
                ),
                "properties", Map.of(
                        "cashflowType", Map.of("type", "string", "enum", List.of("EXPENSE", "ETC")),
                        "recurrenceType", Map.of("type", "string", "enum", List.of("VARIABLE", "ONE_TIME")),
                        "categoryName", Map.of("type", "string", "enum", allowedCategories),
                        "memoOwner", Map.of("type", "string", "enum", allowedOwners),
                        "recommendedMemo", Map.of("type", "string"),
                        "confidence", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                        "reason", Map.of("type", "string"),
                        "BusinessType", Map.of("type", "string")
                )
        );
    }

    private void validateResult(
            OnnuriMemoClassificationResult result,
            List<String> allowedCategories,
            List<String> allowedOwners
    ) {
        if (!allowedCategories.contains(result.categoryName())) {
            throw new IllegalStateException("LLM returned a category that is not allowed. categoryName="
                    + result.categoryName());
        }
        if (!allowedOwners.contains(result.memoOwner())) {
            throw new IllegalStateException("LLM returned a memo owner that is not allowed. memoOwner="
                    + result.memoOwner());
        }
        if (!StringUtils.hasText(result.recommendedMemo())) {
            throw new IllegalStateException("LLM returned an empty recommendedMemo.");
        }
    }

    private List<String> normalizeList(List<String> values, List<String> defaults) {
        if (values == null || values.isEmpty()) {
            return defaults;
        }
        List<String> normalized = values.stream()
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        if (normalized.isEmpty()) {
            return defaults;
        }
        return normalized;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to create LLM request JSON.", e);
        }
    }
}
