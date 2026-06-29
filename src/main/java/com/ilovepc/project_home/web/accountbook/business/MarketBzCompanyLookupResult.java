package com.ilovepc.project_home.web.accountbook.business;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketBzCompanyLookupResult {
    private String businessNumber;
    private String sourceUrl;
    private int httpStatus;
    private int contentLength;
    private boolean containsIndustryClassification;
    private String bzType;
    private String industrySectionText;
}
