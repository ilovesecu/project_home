package com.ilovepc.project_home.web.accountbook.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ilovepc.project_home.config.gemini.GeminiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiInteractionsClient {
    private final GeminiProperties geminiProperties;
    private final ObjectMapper objectMapper;

    public String createStructuredResponse(String input, Map<String, Object> responseSchema) {
        if (!StringUtils.hasText(geminiProperties.getApiKey())) {
            throw new IllegalStateException("GEMINI_API_KEY environment variable is not configured.");
        }

        List<String> models = resolveModels();
        IllegalStateException lastRetryableException = null;
        log.info("GEMINI STRUCTURED RESPONSE MODEL CANDIDATES={}", models);

        for (String model : models) {
            try {
                return sendStructuredResponseRequest(model, input, responseSchema);
            } catch (GeminiRetryableException e) {
                lastRetryableException = new IllegalStateException(e.getMessage(), e);
            }
        }

        throw lastRetryableException == null
                ? new IllegalStateException("No Gemini model is configured.")
                : lastRetryableException;
    }

    private String sendStructuredResponseRequest(String model, String input, Map<String, Object> responseSchema) {
        try {
            log.info("GEMINI STRUCTURED RESPONSE REQUEST MODEL={}, inputLength={}", model, input.length());
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "input", input,
                    "response_format", Map.of(
                            "type", "text",
                            "mime_type", "application/json",
                            "schema", responseSchema
                    )
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(geminiProperties.getBaseUrl() + "/interactions"))
                    .timeout(Duration.ofMillis(geminiProperties.getTimeoutMs()))
                    .header("x-goog-api-key", geminiProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                String message = "Gemini API call failed. model=" + model
                        + ", status=" + response.statusCode()
                        + ", body=" + response.body();
                if (isRetryableStatus(response.statusCode())) {
                    throw new GeminiRetryableException(message);
                }
                throw new IllegalStateException(message);
            }

            return extractOutputText(response.body());
        } catch (IOException e) {
            throw new IllegalStateException("Failed while processing Gemini API request or response. model=" + model, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gemini API request was interrupted. model=" + model, e);
        }
    }

    private List<String> resolveModels() {
        List<String> models = new ArrayList<>();
        addModelIfPresent(models, geminiProperties.getModel());
        if (geminiProperties.getFallbackModels() != null) {
            geminiProperties.getFallbackModels().forEach(model -> addModelIfPresent(models, model));
        }
        return models;
    }

    private void addModelIfPresent(List<String> models, String model) {
        if (StringUtils.hasText(model) && !models.contains(model.trim())) {
            models.add(model.trim());
        }
    }

    private boolean isRetryableStatus(int statusCode) {
        return statusCode == 429 || statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504;
    }

    private String extractOutputText(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);

        if (StringUtils.hasText(root.path("output_text").asText(null))) {
            return root.path("output_text").asText();
        }
        if (root.has("cashflowType")) {
            return responseBody;
        }

        String outputText = extractFromOutput(root);
        if (StringUtils.hasText(outputText)) {
            return outputText;
        }

        String stepText = extractFromSteps(root);
        if (StringUtils.hasText(stepText)) {
            return stepText;
        }

        String candidateText = root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText(null);
        if (StringUtils.hasText(candidateText)) {
            return candidateText;
        }

        throw new IllegalStateException("Gemini response does not contain output_text. body=" + responseBody);
    }

    private String extractFromSteps(JsonNode root) {
        StringBuilder outputText = new StringBuilder();
        for (JsonNode step : root.path("steps")) {
            if (!"model_output".equals(step.path("type").asText())) {
                continue;
            }
            for (JsonNode content : step.path("content")) {
                String text = content.path("text").asText(null);
                if (StringUtils.hasText(text)) {
                    outputText.append(text);
                }
            }
        }
        return outputText.toString();
    }

    private String extractFromOutput(JsonNode root) {
        StringBuilder outputText = new StringBuilder();
        for (JsonNode output : root.path("output")) {
            for (JsonNode content : output.path("content")) {
                String text = content.path("text").asText(null);
                if (StringUtils.hasText(text)) {
                    outputText.append(text);
                }
            }
        }
        return outputText.toString();
    }

    private static class GeminiRetryableException extends RuntimeException {
        private GeminiRetryableException(String message) {
            super(message);
        }
    }
}