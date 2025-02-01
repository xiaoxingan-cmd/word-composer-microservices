package com.shayakum.CardRepresentorService.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shayakum.CardRepresentorService.utils.resources.YamlPropertySourceFactory;
import okhttp3.*;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource(value = "classpath:auth.yml", factory = YamlPropertySourceFactory.class)
public class PublicImageService {
    private final Logger logger = LoggerFactory.getLogger(PublicImageService.class);
    private final OkHttpClient httpClient = new OkHttpClient();
    ObjectMapper objectMapper = new ObjectMapper();

    @Value("${imgbb.apiName}")
    private String API_NAME;
    @Value("${imgbb.apiUrl}")
    private String API_URL;
    @Value("${imgbb.apiKey}")
    private String API_KEY;

    public String publicImage(String base64) {
        logger.info("Has started to publish image in " + API_NAME);
        String url = API_URL + API_KEY;

        RequestBody formBody = new FormBody.Builder()
                .add("image", base64)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "multipart / form-data")
                .post(formBody)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                logger.error("Unexpected result during publishing an image: " + response);
                throw new HttpException(response.message());
            }

            return getPublicUrl(response.body().string());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getPublicUrl(String response) {
        try {
            JsonNode rootNode = objectMapper.readTree(response);
            return rootNode.get("data").get("url").asText();
        } catch (JsonProcessingException e) {
            logger.error("Error while parsing JSON from " + API_NAME, e);
            return null;
        }
    }
}
