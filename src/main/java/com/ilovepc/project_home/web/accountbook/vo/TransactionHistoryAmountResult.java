package com.ilovepc.project_home.web.accountbook.vo;

import lombok.Data;

@Data
public class TransactionHistoryAmountResult {
    private long totalAmount; //입금+출금
    private long incomeAmount; //입금 (수입)
    private long expenditureAmount; //출금 (지출)
    private TransactionHistoryResult maxExpenditureObj; //최고 지출항목
    private TransactionHistoryResult minExpenditureObj; //최고 수입항목
}
