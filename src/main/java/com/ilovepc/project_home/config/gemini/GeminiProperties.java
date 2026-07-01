package com.ilovepc.project_home.config.gemini;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gemini")
public class GeminiProperties {
    private String apiKey;
    private String baseUrl = "https://generativelanguage.googleapis.com/v1beta";
    private String model = "gemini-3.5-flash";
    private List<String> fallbackModels = List.of("gemini-3.1-flash-lite", "gemini-2.5-flash-lite");
    private int timeoutMs = 120000;
}