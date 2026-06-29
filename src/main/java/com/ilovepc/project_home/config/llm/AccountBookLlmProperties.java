package com.ilovepc.project_home.config.llm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "account-book.llm")
public class AccountBookLlmProperties {
    private String provider = "gemini";
}
