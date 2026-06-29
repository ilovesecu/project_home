package com.ilovepc.project_home.web.accountbook.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.config.openai.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiResponsesClient {
    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;

    public String createStructuredResponse(Map<String, Object> requestBody) {
        if (!StringUtils.hasText(openAiProperties.getApiKey())) {
            throw new IllegalStateException("OPENAI_API_KEY 환경변수가 설정되어 있지 않습니다.");
        }

        try {
            String body = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(openAiProperties.getBaseUrl() + "/responses"))
                    .timeout(Duration.ofMillis(openAiProperties.getTimeoutMs()))
                    .header("Authorization", "Bearer " + openAiProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("OpenAI API 호출에 실패했습니다. status=" + response.statusCode()
                        + ", body=" + response.body());
            }

            return extractOutputText(response.body());
        } catch (IOException e) {
            throw new IllegalStateException("OpenAI API 요청/응답 처리 중 오류가 발생했습니다.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("OpenAI API 요청이 중단되었습니다.", e);
        }
    }

    private String extractOutputText(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        StringBuilder outputText = new StringBuilder();
        for (JsonNode output : root.path("output")) {
            for (JsonNode content : output.path("content")) {
                if ("output_text".equals(content.path("type").asText())) {
                    outputText.append(content.path("text").asText());
                }
            }
        }
        if (!StringUtils.hasText(outputText.toString())) {
            throw new IllegalStateException("OpenAI 응답에서 output_text를 찾지 못했습니다.");
        }
        return outputText.toString();
    }
}
