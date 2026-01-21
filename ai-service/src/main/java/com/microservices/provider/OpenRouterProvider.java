package com.microservices.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;

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

    private final ChatClient chatClient;
    private final String modelName;

    public OpenRouterProvider(
            ChatClient chatClient,
            @Value("${ai.model:google/gemini-pro}") String modelName
    ) {
        this.chatClient = chatClient;
        this.modelName = modelName;
        log.info("OpenRouter AI Provider initialized with model {}", modelName);
    }

    @Override
    public String generate(String userPrompt) {
        return generateWithContext(null, userPrompt);
    }

    @Override
    public String generateWithContext(String systemPrompt, String userPrompt) {
        try {
            Prompt prompt;

            if (systemPrompt != null && !systemPrompt.isBlank()) {
                prompt = new Prompt(List.of(
                        new SystemMessage(systemPrompt),
                        new UserMessage(userPrompt)
                ));
            } else {
                prompt = new Prompt(userPrompt);
            }

            return chatClient.prompt(prompt)
                    .call()
                    .content();

        } catch (Exception e) {
            log.error("OpenRouter AI error", e);
            throw new RuntimeException("Failed to generate AI response", e);
        }
    }

    @Override
    public String getProviderName() {
        return "openrouter-" + modelName;
    }
}