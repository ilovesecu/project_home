package com.ilovepc.project_home.web.wakeup.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wakeUp")
@Slf4j
public class WakeUpController {
    @PostMapping("")
    public String wakeup() {
        log.error("wakeUp");
        return "okay";
    }
}
