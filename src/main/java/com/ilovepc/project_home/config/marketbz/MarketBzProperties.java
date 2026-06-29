package com.ilovepc.project_home.config.marketbz;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "account-book.market-bz")
public class MarketBzProperties {
    private String baseUrl = "https://marketbz.com";
    private int timeoutMs = 10000;
}
