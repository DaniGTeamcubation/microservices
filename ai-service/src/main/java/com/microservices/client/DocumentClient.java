package com.microservices.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "document-service")
public interface DocumentClient {

    @GetMapping("/api/documents/{id}")
    Map<String, Object> getDocumentById(@PathVariable("id") Long id);

}