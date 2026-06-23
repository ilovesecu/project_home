package com.ilovepc.project_home.web.accountbook.classification;

import com.ilovepc.project_home.web.accountbook.vo.TransactionHistoryParam;

import java.util.List;

//분류 완료된 거래 리스트와 warning 개수를 같이 반환하는 작은 record입니다.
public record TransactionMemoClassificationResult(
        List<TransactionHistoryParam> transactions,
        int warningCount
) {
}
