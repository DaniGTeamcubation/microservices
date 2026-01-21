package com.microservices.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservices.ai.config.AiConfig;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OpenRouterService {

    private final OkHttpClient httpClient;
    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public OpenRouterService(OkHttpClient httpClient, AiConfig aiConfig) {
        this.httpClient = httpClient;
        this.aiConfig = aiConfig;
        this.objectMapper = new ObjectMapper();
    }

    public String chatCompletion(String systemPrompt, String userMessage) throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getModel());

        List<Map<String, String>> messages = List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
        );
        requestBody.put("messages", messages);
        requestBody.put("temperature", aiConfig.getTemperature());
        requestBody.put("max_tokens", aiConfig.getMaxTokens());

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        Request request = new Request.Builder()
                .url(aiConfig.getBaseUrl() + "/chat/completions")
                .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .addHeader("HTTP-Referer", "http://localhost:8090")
                .addHeader("X-Title", "IA-Service")
                .post(RequestBody.create(jsonBody, JSON))
                .build();

        log.info("Sending request to OpenRouter - Model: {}", aiConfig.getModel());

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error body";
                log.error("OpenRouter API error: {} - {}", response.code(), errorBody);
                throw new IOException("Unexpected code " + response + " - " + errorBody);
            }

            String responseBody = response.body().string();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            String content = jsonNode
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            log.info("Received response from OpenRouter - Length: {} chars", content.length());
            return content;
        }
    }

    public String chatCompletion(String userMessage) throws IOException {
        return chatCompletion("You are a helpful AI assistant.", userMessage);
    }
}