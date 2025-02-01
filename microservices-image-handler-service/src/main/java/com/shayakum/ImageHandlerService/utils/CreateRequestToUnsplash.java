package com.shayakum.ImageHandlerService.utils;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CreateRequestToUnsplash {
    public String findImage(String query, String accessKey) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Authorization", "Client-ID " + accessKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(httpHeaders);

        String url = "https://api.unsplash.com/search/photos?page=1&per_page=1&orientation=squarish&query=" + query;
        
        ResponseEntity<String> response = restTemplate.exchange(
                url,                  
                HttpMethod.GET,       
                requestEntity,        
                String.class          
        );
        
        return response.getBody();
    }

    public byte[] downloadImage(String url) {
        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.getForObject(url, byte[].class);
    }
}
