package com.shayakum.CardComposerService.utils;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Component
public class KafkaRequest {
    public final String CONFIGURE_AND_SUBSCRIBE_HEADER = "application/vnd.kafka.v2+json";
    public final String RETRIEVE_AND_PRODUCE_MESSAGES_HEADER = "application/vnd.kafka.json.v2+json";

    public ResponseEntity<String> postForEntity(Map<String, Object> jsonToSend, String url, String httpHeader) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf(httpHeader));

        HttpEntity<Map<?, ?>> request = new HttpEntity<>(jsonToSend, httpHeaders);

        return restTemplate.postForEntity(url, request, String.class);
    }

    public String getForObject(String url, String httpHeader) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.valueOf(httpHeader)));

        HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }
}
