package com.shayakum.ImageHandlerService.services;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shayakum.ImageHandlerService.utils.CreateRequestToUnsplash;
import com.shayakum.ImageHandlerService.utils.resources.YamlPropertySourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

@Service
@PropertySource(value = "classpath:auth.yml", factory = YamlPropertySourceFactory.class)
public class SeekForImageService {
    @Value("${unsplash.accessKey}")
    private String accessKey;

    private final AWSS3Service awss3Service;
    private final WordDetailsService wordDetailsService;
    private final CreateRequestToUnsplash createRequestToUnsplash;
    private final Logger logger = LoggerFactory.getLogger(SeekForImageService.class);

    @Autowired
    public SeekForImageService(AWSS3Service awss3Service, WordDetailsService wordDetailsService, CreateRequestToUnsplash createRequestToUnsplash) {
        this.awss3Service = awss3Service;
        this.wordDetailsService = wordDetailsService;
        this.createRequestToUnsplash = createRequestToUnsplash;
    }

    public boolean searchImage(String message) {
        logger.info("Beginning to search an image");
        try {
            String apiResponse = createRequestToUnsplash.findImage(message, accessKey);
            JsonNode jsonNode = new ObjectMapper().readTree(apiResponse);
            String imageUrl = jsonNode.path("results").get(0).path("urls").path("small").asText();

            byte[] image = createRequestToUnsplash.downloadImage(imageUrl);

            return createRequestToS3(image, message);
        } catch (Exception e) {
            logger.error("Error searching an image", e);
            return false;
        }
    }

    private boolean createRequestToS3(byte[] image, String query) {
        ByteArrayInputStream imageStream = new ByteArrayInputStream(image);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/" + "jpg");
        metadata.setContentLength(image.length);

        try {
            logger.info("Uploading object in a S3Container...");
            awss3Service.uploadObject(query, imageStream, metadata);
            logger.info("Changing 'S3Value' in a Database...");
            return wordDetailsService.setS3Value(query, true);
        } catch (Exception e) {
            logger.error("Have got an error while working with a S3Container or Database", e);
            return false;
        }
    }
}
