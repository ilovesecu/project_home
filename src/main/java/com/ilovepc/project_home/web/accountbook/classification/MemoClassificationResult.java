package com.ilovepc.project_home.web.accountbook.classification;

import com.ilovepc.project_home.web.accountbook.vo.CashFlowType;
import com.ilovepc.project_home.web.accountbook.vo.MemoParseStatus;
import com.ilovepc.project_home.web.accountbook.vo.RecurrenceType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemoClassificationResult {
    //메모 파싱 결과를 담는 객체입니다. 파싱 성공 여부, 실패 사유, 카테고리명, 주체, 본문 등을 담습니다.
    private MemoParseStatus memoParseStatus;
    private String message;
    private CashFlowType cashFlowType;
    private RecurrenceType recurrenceType;
    private String categoryName;
    private String memoOwner;
    private String memoTargetYearMonth;

    public boolean isParsed() {
        return memoParseStatus == MemoParseStatus.PARSED;
    }
}
