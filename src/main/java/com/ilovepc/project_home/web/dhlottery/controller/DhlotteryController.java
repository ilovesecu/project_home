package com.ilovepc.project_home.web.dhlottery.controller;

import com.ilovepc.project_home.common.vo.ApiResponse;
import com.ilovepc.project_home.web.dhlottery.service.DhlotteryBotService;
import com.ilovepc.project_home.web.dhlottery.vo.LotteryGameHistoryResponse;
import com.ilovepc.project_home.web.dhlottery.vo.LottoLedgerSearchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dhlottery")
@RequiredArgsConstructor
@Slf4j
public class DhlotteryController {
    private final DhlotteryBotService dhlotteryBotService;
    @Value("${externalCredencial.dhlottery.secretId}")
    private String secretId;
    @Value("${externalCredencial.dhlottery.secretPW}")
    private String secretPW;


    @GetMapping("/myGameHistory")
    public ApiResponse<?> getMyGameHistory(LottoLedgerSearchVO searchVO) {
        searchVO.fillDefaultValues(); //없는 값을 기본값 채우기
        LotteryGameHistoryResponse response = dhlotteryBotService.getLedger(secretId, secretPW, searchVO);
        return ApiResponse.success(response);
    }
}
