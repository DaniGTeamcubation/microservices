package com.microservices.ai.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "openrouter", matchIfMissing = true)
@Slf4j
public class OpenRouterProvider implements AIProvider {

    private final ChatModel chatModel;

    @Value("${ai.model:google/gemini-pro}")
    private String modelName;

    public OpenRouterProvider(ChatModel chatModel) {
        this.chatModel = chatModel;
        log.info("OpenRouter AI Provider initialized");
    }

    @Override
    public String generate(String prompt) {
        log.info("Generating response with OpenRouter ({}) - Prompt length: {} chars",
                modelName, prompt.length());

        try {
            String response = chatModel.call(prompt);

            log.info("OpenRouter response received - Length: {} chars", response.length());
            return response;

        } catch (Exception e) {
            log.error("Error calling OpenRouter API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate response from OpenRouter", e);
        }
    }

    @Override
    public String generateWithContext(String systemPrompt, String userPrompt) {
        log.info("Generating response with OpenRouter ({}) (with context)", modelName);
        log.debug("System prompt: {}", systemPrompt);
        log.debug("User prompt: {}", userPrompt);

        try {
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userPrompt)
            ));

            String response = chatModel.call(prompt).getResult().getOutput().getContent();

            log.info("OpenRouter response received - Length: {} chars", response.length());
            return response;

        } catch (Exception e) {
            log.error("Error calling OpenRouter API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate response from OpenRouter", e);
        }
    }

    @Override
    public String getProviderName() {
        return "openrouter-" + modelName;
    }
}