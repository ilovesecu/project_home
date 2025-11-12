package com.ilovepc.project_home.web.auth.controller;

import com.ilovepc.project_home.web.auth.service.SignUpService;
import com.ilovepc.project_home.web.auth.vo.signup.SignUpRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    // SecurityConfig에서 직접 AuthenticationManager를 Bean으로 등록하고 주입받아야 합니다.
    // (이 예제에서는 생략되었으나, 실제 구현 시 필요)
    // 여기서는 간단히 Spring Security의 기본 AuthenticationManager를 사용한다고 가정합니다.
    // 하지만 JWT에서는 Custom AuthenticationProvider나 직접 인증 로직이 필요할 수 있습니다.
    private final SignUpService signUpService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @PostMapping("/v1/signup")
    public String signup(@Valid @RequestBody SignUpRequest signUpRequest) {
        signUpService.signUp(signUpRequest);
        return "signup";
    }
}
