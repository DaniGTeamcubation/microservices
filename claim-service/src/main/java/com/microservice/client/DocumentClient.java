package com.microservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@FeignClient(name = "document-service")
public interface DocumentClient {

    @PostMapping(value = "/api/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    Map<String, Object> uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam("claimId") Long claimId
    );

    @GetMapping("/api/documents/claim/{claimId}")
    List<Map<String, Object>> getDocumentsByClaimId(@PathVariable("claimId") Long claimId);
}
