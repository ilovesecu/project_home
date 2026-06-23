package com.ilovepc.project_home.web.accountbook.classification;

import com.ilovepc.project_home.web.accountbook.vo.CashFlowType;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
public class AccountBookMemoRules {
    //어떤 태그를 정상 태그로 볼지 들고 있는 규칙 객체입니다.
    //지출/수입/투자/저축/이체/기타, 고정/변동/일회성, 수입 카테고리, 지출 카테고리 목록을 관리합니다.
    private static final Set<String> DEFAULT_MAIN_TAGS = Set.of("지출", "수입", "저축", "투자", "이체", "기타");
    private static final Set<String> DEFAULT_RECURRENCE_TAGS = Set.of("고정", "변동", "일회성");

    private final Set<String> mainTags;
    private final Set<String> incomeCategoryTags;
    private final Set<String> nonIncomeCategoryTags;
    private final Set<String> recurrenceTags;

    private AccountBookMemoRules(
            Set<String> mainTags,
            Set<String> incomeCategoryTags,
            Set<String> nonIncomeCategoryTags,
            Set<String> recurrenceTags
    ) {
        this.mainTags = freezeOrDefault(mainTags, DEFAULT_MAIN_TAGS);
        this.incomeCategoryTags = freeze(incomeCategoryTags);
        this.nonIncomeCategoryTags = freeze(nonIncomeCategoryTags);
        this.recurrenceTags = freezeOrDefault(recurrenceTags, DEFAULT_RECURRENCE_TAGS);
    }

    public static AccountBookMemoRules defaults() {
        return new AccountBookMemoRules(
                DEFAULT_MAIN_TAGS,
                Collections.emptySet(),
                Collections.emptySet(),
                DEFAULT_RECURRENCE_TAGS
        );
    }

    public static AccountBookMemoRules of(
            Set<String> mainTags,
            Set<String> incomeCategoryTags,
            Set<String> nonIncomeCategoryTags,
            Set<String> recurrenceTags
    ) {
        return new AccountBookMemoRules(mainTags, incomeCategoryTags, nonIncomeCategoryTags, recurrenceTags);
    }

    public CashFlowType cashFlowTypeOf(String tag) {
        return switch (tag) {
            case "수입" -> CashFlowType.INCOME;
            case "지출" -> CashFlowType.EXPENSE;
            case "투자" -> CashFlowType.INVESTMENT;
            case "저축" -> CashFlowType.SAVING;
            case "이체" -> CashFlowType.TRANSFER;
            case "기타" -> CashFlowType.ETC;
            default -> CashFlowType.NONE;
        };
    }

    public boolean isKnownMainTag(String tag) {
        return mainTags.contains(tag) && cashFlowTypeOf(tag) != CashFlowType.NONE;
    }

    public boolean isKnownRecurrenceTag(String tag) {
        return recurrenceTags.contains(tag);
    }

    public boolean isKnownCategory(CashFlowType cashFlowType, String categoryName) {
        if (!StringUtils.hasText(categoryName)) {
            return false;
        }
        if (!isCategoryValidationEnabled()) {
            return true;
        }
        if (cashFlowType == CashFlowType.INCOME) {
            return incomeCategoryTags.contains(categoryName);
        }
        if (cashFlowType == CashFlowType.NONE) {
            return false;
        }
        return nonIncomeCategoryTags.contains(categoryName);
    }

    public boolean isCategoryValidationEnabled() {
        return !incomeCategoryTags.isEmpty() || !nonIncomeCategoryTags.isEmpty();
    }

    private static Set<String> freezeOrDefault(Set<String> values, Set<String> defaults) {
        if (values == null || values.isEmpty()) {
            return Collections.unmodifiableSet(new LinkedHashSet<>(defaults));
        }
        return freeze(values);
    }

    private static Set<String> freeze(Set<String> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(new LinkedHashSet<>(values));
    }
}
