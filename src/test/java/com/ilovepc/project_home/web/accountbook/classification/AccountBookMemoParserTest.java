package com.ilovepc.project_home.web.accountbook.classification;

import com.ilovepc.project_home.web.accountbook.vo.CashFlowType;
import com.ilovepc.project_home.web.accountbook.vo.MemoParseStatus;
import com.ilovepc.project_home.web.accountbook.vo.RecurrenceType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AccountBookMemoParserTest {
    private final AccountBookMemoParser parser = new AccountBookMemoParser();
    private final AccountBookMemoRules rules = AccountBookMemoRules.of(
            Set.of("지출", "수입", "저축", "투자", "이체", "기타"),
            Set.of("급여", "상여", "부업", "이자", "환급", "기타수입"),
            Set.of("식비", "생활", "주거", "보험", "증권", "청약", "기타"),
            Set.of("고정", "변동", "일회성")
    );

    @Test
    void parseFullMemoTags() {
        MemoClassificationResult result = parser.parse("[지출][변동][식비] @공동 온누리 결제 식료품", rules);

        assertThat(result.getMemoParseStatus()).isEqualTo(MemoParseStatus.PARSED);
        assertThat(result.getCashFlowType()).isEqualTo(CashFlowType.EXPENSE);
        assertThat(result.getRecurrenceType()).isEqualTo(RecurrenceType.VARIABLE);
        assertThat(result.getCategoryName()).isEqualTo("식비");
        assertThat(result.getMemoOwner()).isEqualTo("@공동");
    }

    @Test
    void parseMemoWithoutRecurrenceAsVariable() {
        MemoClassificationResult result = parser.parse("[지출][식비] @승주 점심", rules);

        assertThat(result.getMemoParseStatus()).isEqualTo(MemoParseStatus.PARSED);
        assertThat(result.getRecurrenceType()).isEqualTo(RecurrenceType.VARIABLE);
        assertThat(result.getCategoryName()).isEqualTo("식비");
        assertThat(result.getMemoOwner()).isEqualTo("@승주");
    }

    @Test
    void extractTargetYearMonth() {
        MemoClassificationResult result = parser.parse("[수입][고정][급여] @성은 2605 급여", rules);

        assertThat(result.getMemoParseStatus()).isEqualTo(MemoParseStatus.PARSED);
        assertThat(result.getCashFlowType()).isEqualTo(CashFlowType.INCOME);
        assertThat(result.getRecurrenceType()).isEqualTo(RecurrenceType.FIXED);
        assertThat(result.getMemoTargetYearMonth()).isEqualTo("2026-05");
    }

    @Test
    void blankMemoIsWarningTarget() {
        MemoClassificationResult result = parser.parse("", rules);

        assertThat(result.getMemoParseStatus()).isEqualTo(MemoParseStatus.BLANK);
    }

    @Test
    void invalidMemoIsWarningTarget() {
        MemoClassificationResult result = parser.parse("???", rules);

        assertThat(result.getMemoParseStatus()).isEqualTo(MemoParseStatus.INVALID_FORMAT);
    }

    @Test
    void unknownCategoryIsWarningTarget() {
        MemoClassificationResult result = parser.parse("[수입][기타] @공동 s2통장 잔액", rules);

        assertThat(result.getMemoParseStatus()).isEqualTo(MemoParseStatus.UNKNOWN_TAG);
    }
}
