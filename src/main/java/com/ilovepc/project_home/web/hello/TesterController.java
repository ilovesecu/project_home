package com.ilovepc.project_home.web.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TesterController {
    @GetMapping("/api/user/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/api/admin/hello")
    public String adminHello() {
        return "Hello World";
    }

    @GetMapping("/api/guest/hello")
    public String guestHello() {
        return "Hello World";
    }
}
