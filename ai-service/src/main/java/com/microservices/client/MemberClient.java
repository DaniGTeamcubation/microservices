package com.microservices.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "member-service")
public interface MemberClient {

    @GetMapping("/api/members/{id}")
    Map<String, Object> getMemberById(@PathVariable("id") Long id);

}