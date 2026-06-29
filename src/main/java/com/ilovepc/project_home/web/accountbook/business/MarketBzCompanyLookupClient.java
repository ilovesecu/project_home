package com.ilovepc.project_home.web.accountbook.business;

import com.ilovepc.project_home.config.marketbz.MarketBzProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class MarketBzCompanyLookupClient {
    private final MarketBzProperties marketBzProperties;

    public MarketBzHtmlResponse fetchCompanyDetail(String businessNumber) {
        String sourceUrl = marketBzProperties.getBaseUrl() + "/companyDetail/" + businessNumber;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sourceUrl))
                    .timeout(Duration.ofMillis(marketBzProperties.getTimeoutMs()))
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .header("User-Agent", "Mozilla/5.0 AccountBookBot/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            return new MarketBzHtmlResponse(sourceUrl, response.statusCode(), response.body());
        } catch (IOException e) {
            throw new IllegalStateException("MarketBz company detail request failed. businessNumber="
                    + businessNumber, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("MarketBz company detail request was interrupted. businessNumber="
                    + businessNumber, e);
        }
    }

    public record MarketBzHtmlResponse(
            String sourceUrl,
            int httpStatus,
            String html
    ) {
    }
}
