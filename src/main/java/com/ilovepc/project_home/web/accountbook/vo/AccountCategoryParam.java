package com.ilovepc.project_home.web.accountbook.vo;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"cashflowType", "name"})
public class AccountCategoryParam {
    private final String cashflowType;
    private final String name;
    private final Integer sortOrder;

    public AccountCategoryParam(String cashflowType, String name, Integer sortOrder) {
        this.cashflowType = cashflowType;
        this.name = name;
        this.sortOrder = sortOrder;
    }
}
