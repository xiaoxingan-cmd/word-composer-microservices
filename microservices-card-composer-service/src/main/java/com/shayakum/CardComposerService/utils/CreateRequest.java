package com.shayakum.CardComposerService.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class CreateRequest {
    public String postForObject(Map<String, String> jsonToSend, String url) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<?, ?>> request = new HttpEntity<>(jsonToSend, httpHeaders);

        return restTemplate.postForObject(url, request, String.class);
    }

    public String getForObject(String url) {
        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.getForObject(url, String.class);
    }
}
