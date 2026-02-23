package com.ilovepc.project_home.web.dhlottery.controller;

import com.ilovepc.project_home.web.dhlottery.service.DhlotteryBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dhlottery")
@RequiredArgsConstructor
@Slf4j
public class DhlotteryController {
    private final DhlotteryBotService dhlotteryBotService;


    @GetMapping("/test")
    public String test(){
        String bonobono94 = dhlotteryBotService.getLedger("bonobono94", "Wjdtmdwn94!");
        return bonobono94;
    }
}
