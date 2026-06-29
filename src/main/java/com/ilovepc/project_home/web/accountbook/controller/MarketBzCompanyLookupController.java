package com.ilovepc.project_home.web.accountbook.controller;

import com.ilovepc.project_home.common.vo.ApiResponse;
import com.ilovepc.project_home.web.accountbook.business.MarketBzCompanyLookupResult;
import com.ilovepc.project_home.web.accountbook.business.MarketBzCompanyLookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/account-book/businesses")
public class MarketBzCompanyLookupController {
    /*
        현재 해당 서비스는 일반 파싱으로는 해결 안될거같아서 보류상태입니다. (주는 HTML에 원하는 값이 없고, JS로 설정하는 것으로 판단됨)
    */

    private final MarketBzCompanyLookupService marketBzCompanyLookupService;

    @GetMapping("/{businessNumber}/market-bz")
    public ApiResponse<MarketBzCompanyLookupResult> lookupMarketBz(
            @PathVariable String businessNumber
    ) {
        log.info("MARKETBZ COMPANY LOOKUP COMMAND EXEC : businessNumber={}", businessNumber);
        return ApiResponse.success(marketBzCompanyLookupService.lookup(businessNumber));
    }
}
