package com.ilovepc.project_home.web.accountbook.classification;

import com.ilovepc.project_home.web.accountbook.vo.CashFlowType;
import com.ilovepc.project_home.web.accountbook.vo.MemoParseStatus;
import com.ilovepc.project_home.web.accountbook.vo.RecurrenceType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AccountBookMemoParser {
    //메모 문자열을 실제 구조로 쪼개는 순수 파서입니다.
    //예: [지출][고정][보험] @성은 보험1 2606 → 대분류 EXPENSE, 반복유형 FIXED, 카테고리 보험, 주체 @성은, 본문 보험1 2606, 대상월 2026-06.

    private static final Pattern MEMO_PATTERN = Pattern.compile("^((?:\\[[^\\]]+\\])+)[ \\t]*(?:(@\\S+)[ \\t]*)?(.*)$");    //  메모 전체를 태그 / 주체 / 본문으로 나눔
    private static final Pattern TAG_PATTERN = Pattern.compile("\\[([^\\]]+)]");                                            //  [지출][고정][보험]에서 지출, 고정, 보험 추출
    private static final Pattern TARGET_MONTH_PATTERN = Pattern.compile("(?<!\\d)(\\d{4})(?!\\d)");                         //  본문 등에서 2606 같은 대상월 추출

    public MemoClassificationResult parse(String memo, AccountBookMemoRules rules) {
        if (!StringUtils.hasText(memo)) {
            return failed(MemoParseStatus.BLANK, "메모가 비어 있습니다.");
        }

        String trimmedMemo = memo.trim();
        Matcher memoMatcher = MEMO_PATTERN.matcher(trimmedMemo); //[태그들]  @주체  본문 로 나눈다. ->  group(1) = [지출][고정][보험]   group(2) = @성은     group(3) = 보험1 2606
        if (!memoMatcher.matches()) {
            return failed(MemoParseStatus.INVALID_FORMAT, "메모 태그 형식이 올바르지 않습니다.");
        }

        List<String> tags = extractTags(memoMatcher.group(1)); //[태그들] : [지출][고정][보험]    ->         지출 고정 보험 으로 대괄호는 제거하고 안쪽값만 리스트로 만든다.
        if (tags.isEmpty()) {
            return failed(MemoParseStatus.INVALID_FORMAT, "메모 태그가 없습니다.");
        }

        String mainTag = tags.get(0);
        if (!rules.isKnownMainTag(mainTag)) {
            return failed(MemoParseStatus.UNKNOWN_TAG, "알 수 없는 대분류 태그입니다. tag=" + mainTag);
        }

        CashFlowType cashFlowType = rules.cashFlowTypeOf(mainTag);
        int categoryIndex = 1;
        RecurrenceType recurrenceType = RecurrenceType.VARIABLE;
        if (tags.size() > categoryIndex && rules.isKnownRecurrenceTag(tags.get(categoryIndex))) {
            recurrenceType = RecurrenceType.fromTag(tags.get(categoryIndex));
            categoryIndex++;
        }

        if (tags.size() <= categoryIndex) {
            return failed(MemoParseStatus.INVALID_FORMAT, "카테고리 태그가 없습니다.");
        }

        String categoryName = tags.get(categoryIndex);
        if (!rules.isKnownCategory(cashFlowType, categoryName)) {
            return failed(MemoParseStatus.UNKNOWN_TAG, "알 수 없는 카테고리 태그입니다. tag=" + categoryName);
        }

        if (tags.size() > categoryIndex + 1) {
            return failed(MemoParseStatus.INVALID_FORMAT, "지원하지 않는 추가 태그가 있습니다.");
        }

        return MemoClassificationResult.builder()
                .memoParseStatus(MemoParseStatus.PARSED)
                .cashFlowType(cashFlowType)
                .recurrenceType(recurrenceType)
                .categoryName(categoryName)
                .memoOwner(blankToNull(memoMatcher.group(2)))
                .memoBody(blankToNull(memoMatcher.group(3)))
                .memoTargetYearMonth(extractTargetYearMonth(trimmedMemo))   // [수입][고정][급여] @성은 2605 급여 와 같은 메모 안에서 2605 2606 같은 4자리 숫자를 찾는다.    ->         2605 -> 2026-05
                .build();
    }

    private List<String> extractTags(String tagText) {
        List<String> tags = new ArrayList<>();
        Matcher tagMatcher = TAG_PATTERN.matcher(tagText);
        while (tagMatcher.find()) {
            tags.add(tagMatcher.group(1).trim());
        }
        return tags;
    }

    private String extractTargetYearMonth(String memo) {
        Matcher matcher = TARGET_MONTH_PATTERN.matcher(memo);
        while (matcher.find()) {
            String value = matcher.group(1);
            int month = Integer.parseInt(value.substring(2));
            if (month >= 1 && month <= 12) {
                return "20" + value.substring(0, 2) + "-" + value.substring(2);
            }
        }
        return null;
    }

    private MemoClassificationResult failed(MemoParseStatus status, String message) {
        return MemoClassificationResult.builder()
                .memoParseStatus(status)
                .message(message)
                .cashFlowType(CashFlowType.NONE)
                .recurrenceType(RecurrenceType.NONE)
                .build();
    }

    private String blankToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
