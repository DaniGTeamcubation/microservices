package com.microservices.provider;

public interface AIProvider {

    String generate(String prompt);

    String generateWithContext(String systemPrompt, String userPrompt);

    String getProviderName();
}
