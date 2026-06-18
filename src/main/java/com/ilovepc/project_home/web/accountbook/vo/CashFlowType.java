package com.ilovepc.project_home.web.accountbook.vo;

public enum CashFlowType {
    INCOME("수입")
    ,EXPENSE("지출")
    ,INVESTMENT("투자")
    ,SAVING("저축")
    ,ETC("기타")
    ,NONE("미분류")
    ;

    String desc;

    CashFlowType (String desc) {
        this.desc = desc;
    }
}
