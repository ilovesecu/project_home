package com.ilovepc.project_home.web.accountbook.recurrence;

import com.ilovepc.project_home.web.accountbook.vo.AmountProfileType;
import com.ilovepc.project_home.web.accountbook.vo.RecurrenceAmountProfileResult;
import com.ilovepc.project_home.web.accountbook.vo.RecurrenceClassificationSummaryResult;
import com.ilovepc.project_home.web.accountbook.vo.RecurrenceDecisionReason;
import com.ilovepc.project_home.web.accountbook.vo.RecurrenceDecisionResult;
import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 과거 분류 근거와 amount profile을 조합해서 고정/변동/일회성 후보를 점수화하는 역할을 담당합니다.
 */
@Service
public class RecurrenceDecisionService {
    private static final String RECURRENCE_FIXED = "FIXED";
    private static final String RECURRENCE_VARIABLE = "VARIABLE";
    private static final String RECURRENCE_ONE_TIME = "ONE_TIME";
    private static final String STATUS_MANUAL = "MANUAL";
    private static final String STATUS_AUTO = "AUTO";

    /**
     * evidence 목록에서 patternKey를 찾아 반복성 판정을 수행합니다.
     */
    public RecurrenceDecisionResult decide(
            List<TransactionHistoryResult> evidenceTransactions,
            List<RecurrenceClassificationSummaryResult> classificationSummaries,
            RecurrenceAmountProfileResult amountProfile
    ) {
        return decide(
                findPatternKey(evidenceTransactions, classificationSummaries, amountProfile),
                evidenceTransactions,
                classificationSummaries,
                amountProfile
        );
    }

    /**
     * 같은 recurrencePatternKey의 과거 거래, 분류 요약, 금액 특징을 점수로 환산해 추천 반복유형을 반환합니다.
     */
    public RecurrenceDecisionResult decide(
            String recurrencePatternKey,
            List<TransactionHistoryResult> evidenceTransactions,
            List<RecurrenceClassificationSummaryResult> classificationSummaries,
            RecurrenceAmountProfileResult amountProfile
    ) {
        EvidenceCounts evidenceCounts = countEvidence(evidenceTransactions, classificationSummaries);
        RecurrenceCounts recurrenceCounts = countRecurrenceTypes(classificationSummaries);
        List<RecurrenceDecisionReason> reasons = new ArrayList<>();

        int score = 0;
        score += scoreEvidence(evidenceCounts, reasons);
        score += scoreHistoricalRecurrence(recurrenceCounts, reasons);
        score += scoreAmountProfile(amountProfile, reasons);

        int confidence = clamp(score);
        return RecurrenceDecisionResult.builder()
                .recurrencePatternKey(recurrencePatternKey)
                .recommendedRecurrenceType(resolveRecommendedRecurrenceType(confidence, recurrenceCounts))
                .confidence(confidence)
                .score(score)
                .evidenceCount(evidenceCounts.totalCount())
                .manualEvidenceCount(evidenceCounts.manualCount())
                .autoEvidenceCount(evidenceCounts.autoCount())
                .amountProfileType(amountProfile == null ? null : amountProfile.getAmountProfileType())
                .reasonCodes(List.copyOf(reasons))
                .build();
    }

    /**
     * MANUAL/AUTO 근거 수와 반복 발생 수를 점수로 변환합니다.
     */
    private int scoreEvidence(EvidenceCounts evidenceCounts, List<RecurrenceDecisionReason> reasons) {
        int score = 0;
        if (evidenceCounts.totalCount() < 2) {
            score -= 20;
            reasons.add(RecurrenceDecisionReason.INSUFFICIENT_EVIDENCE);
        } else {
            score += 25;
            reasons.add(RecurrenceDecisionReason.REPEATED_PATTERN);
        }

        if (evidenceCounts.manualCount() > 0) {
            score += 40;
            reasons.add(RecurrenceDecisionReason.MANUAL_EVIDENCE_FOUND);
        } else if (evidenceCounts.autoCount() > 0) {
            score += 15;
            reasons.add(RecurrenceDecisionReason.AUTO_EVIDENCE_FOUND);
        }

        return score;
    }

    /**
     * 과거 분류 요약에서 FIXED/VARIABLE 중 어떤 값이 더 강한지 점수에 반영합니다.
     */
    private int scoreHistoricalRecurrence(RecurrenceCounts recurrenceCounts, List<RecurrenceDecisionReason> reasons) {
        if (recurrenceCounts.fixedCount() > 0
                && recurrenceCounts.fixedCount() >= recurrenceCounts.variableCount()
                && recurrenceCounts.fixedCount() >= recurrenceCounts.oneTimeCount()) {
            reasons.add(RecurrenceDecisionReason.FIXED_HISTORY_FOUND);
            return 30;
        }

        if (recurrenceCounts.variableCount() > recurrenceCounts.fixedCount()) {
            reasons.add(RecurrenceDecisionReason.VARIABLE_HISTORY_FOUND);
            return -10;
        }

        return 0;
    }

    /**
     * amount profile의 강도를 점수에 반영합니다.
     */
    private int scoreAmountProfile(
            RecurrenceAmountProfileResult amountProfile,
            List<RecurrenceDecisionReason> reasons
    ) {
        if (amountProfile == null || amountProfile.getAmountProfileType() == null) {
            reasons.add(RecurrenceDecisionReason.INSUFFICIENT_AMOUNT_DATA);
            return 0;
        }

        AmountProfileType amountProfileType = amountProfile.getAmountProfileType();
        if (amountProfileType == AmountProfileType.EXACT) {
            reasons.add(RecurrenceDecisionReason.EXACT_AMOUNT);
            return 20;
        }
        if (amountProfileType == AmountProfileType.SIMILAR_RANGE) {
            reasons.add(RecurrenceDecisionReason.SIMILAR_AMOUNT);
            return 10;
        }
        if (amountProfileType == AmountProfileType.VARIABLE) {
            reasons.add(RecurrenceDecisionReason.VARIABLE_AMOUNT);
            return -10;
        }

        reasons.add(RecurrenceDecisionReason.INSUFFICIENT_AMOUNT_DATA);
        return 0;
    }

    /**
     * 점수와 과거 recurrenceType 다수결을 바탕으로 추천 반복유형을 결정합니다.
     */
    private String resolveRecommendedRecurrenceType(int confidence, RecurrenceCounts recurrenceCounts) {
        if (confidence >= 70) {
            return RECURRENCE_FIXED;
        }

        if (recurrenceCounts.oneTimeCount() > recurrenceCounts.fixedCount()
                && recurrenceCounts.oneTimeCount() > recurrenceCounts.variableCount()) {
            return RECURRENCE_ONE_TIME;
        }

        if (confidence >= 35) {
            return RECURRENCE_VARIABLE;
        }

        return RECURRENCE_ONE_TIME;
    }

    /**
     * 거래 목록과 요약 목록에서 MANUAL/AUTO 근거 개수를 계산합니다.
     */
    private EvidenceCounts countEvidence(
            List<TransactionHistoryResult> evidenceTransactions,
            List<RecurrenceClassificationSummaryResult> classificationSummaries
    ) {
        int totalCount = evidenceTransactions == null
                ? 0
                : (int) evidenceTransactions.stream().filter(Objects::nonNull).count();
        int manualCount = countEvidenceTransactionsByStatus(evidenceTransactions, STATUS_MANUAL);
        int autoCount = countEvidenceTransactionsByStatus(evidenceTransactions, STATUS_AUTO);

        if (totalCount == 0 && classificationSummaries != null) {
            totalCount = classificationSummaries.stream()
                    .filter(Objects::nonNull)
                    .mapToInt(summary -> safeLongToInt(summary.getEvidenceCount()))
                    .sum();
            manualCount = countSummariesByStatus(classificationSummaries, STATUS_MANUAL);
            autoCount = countSummariesByStatus(classificationSummaries, STATUS_AUTO);
        }

        return new EvidenceCounts(totalCount, manualCount, autoCount);
    }

    /**
     * 요약 목록에서 recurrenceType별 과거 근거 수를 계산합니다.
     */
    private RecurrenceCounts countRecurrenceTypes(List<RecurrenceClassificationSummaryResult> classificationSummaries) {
        if (classificationSummaries == null || classificationSummaries.isEmpty()) {
            return new RecurrenceCounts(0, 0, 0);
        }

        int fixedCount = countSummariesByRecurrenceType(classificationSummaries, RECURRENCE_FIXED);
        int variableCount = countSummariesByRecurrenceType(classificationSummaries, RECURRENCE_VARIABLE);
        int oneTimeCount = countSummariesByRecurrenceType(classificationSummaries, RECURRENCE_ONE_TIME);

        return new RecurrenceCounts(fixedCount, variableCount, oneTimeCount);
    }

    private int countEvidenceTransactionsByStatus(List<TransactionHistoryResult> evidenceTransactions, String status) {
        if (evidenceTransactions == null || evidenceTransactions.isEmpty()) {
            return 0;
        }

        return (int) evidenceTransactions.stream()
                .filter(Objects::nonNull)
                .filter(transaction -> status.equals(transaction.getClassificationStatus()))
                .count();
    }

    private int countSummariesByStatus(
            List<RecurrenceClassificationSummaryResult> classificationSummaries,
            String status
    ) {
        return classificationSummaries.stream()
                .filter(Objects::nonNull)
                .filter(summary -> status.equals(summary.getClassificationStatus()))
                .mapToInt(summary -> safeLongToInt(summary.getEvidenceCount()))
                .sum();
    }

    private int countSummariesByRecurrenceType(
            List<RecurrenceClassificationSummaryResult> classificationSummaries,
            String recurrenceType
    ) {
        return classificationSummaries.stream()
                .filter(Objects::nonNull)
                .filter(summary -> recurrenceType.equals(summary.getRecurrenceType()))
                .mapToInt(summary -> safeLongToInt(summary.getEvidenceCount()))
                .sum();
    }

    /**
     * 여러 입력 중 사용 가능한 첫 번째 recurrencePatternKey를 찾습니다.
     */
    private String findPatternKey(
            List<TransactionHistoryResult> evidenceTransactions,
            List<RecurrenceClassificationSummaryResult> classificationSummaries,
            RecurrenceAmountProfileResult amountProfile
    ) {
        if (amountProfile != null && StringUtils.hasText(amountProfile.getRecurrencePatternKey())) {
            return amountProfile.getRecurrencePatternKey();
        }

        String evidencePatternKey = findPatternKeyFromEvidence(evidenceTransactions);
        if (StringUtils.hasText(evidencePatternKey)) {
            return evidencePatternKey;
        }

        return findPatternKeyFromSummary(classificationSummaries);
    }

    private String findPatternKeyFromEvidence(List<TransactionHistoryResult> evidenceTransactions) {
        if (evidenceTransactions == null || evidenceTransactions.isEmpty()) {
            return null;
        }

        return evidenceTransactions.stream()
                .filter(Objects::nonNull)
                .map(TransactionHistoryResult::getRecurrencePatternKey)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private String findPatternKeyFromSummary(List<RecurrenceClassificationSummaryResult> classificationSummaries) {
        if (classificationSummaries == null || classificationSummaries.isEmpty()) {
            return null;
        }

        return classificationSummaries.stream()
                .filter(Objects::nonNull)
                .map(RecurrenceClassificationSummaryResult::getRecurrencePatternKey)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
    }

    private int safeLongToInt(Long value) {
        if (value == null) {
            return 0;
        }
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return value.intValue();
    }

    private int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private record EvidenceCounts(int totalCount, int manualCount, int autoCount) {
    }

    private record RecurrenceCounts(int fixedCount, int variableCount, int oneTimeCount) {
    }
}
