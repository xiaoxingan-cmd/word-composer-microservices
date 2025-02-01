package com.shayakum.CardComposerService.services;

import com.shayakum.CardComposerService.utils.KafkaRequest;
import com.shayakum.CardComposerService.utils.resources.YamlPropertySourceFactory;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@PropertySource(value = "classpath:auth.yml", factory = YamlPropertySourceFactory.class)
public class KafkaProducerService {
    private final KafkaRequest kafkaRequest;
    private final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Autowired
    public KafkaProducerService(KafkaRequest kafkaRequest) {
        this.kafkaRequest = kafkaRequest;
    }

    @Value("${kafkaProducer.url}")
    private String kafkaUrl;
    @Value("${kafkaProducer.topic}")
    private String kafkaTopic;
    private String kafkaServiceUrl;

    @PostConstruct
    public void init() {
        if (kafkaUrl == null || kafkaUrl.isEmpty()) {
            throw new IllegalStateException("kafkaUrl is not configured properly.");
        }
        kafkaServiceUrl = kafkaUrl + kafkaTopic;
    }

    public void produceMessage(String message) {
        Map<String, Object> jsonToSend = Map.of(
                "records", List.of(Map.of("value", message))
        );

        try {
            ResponseEntity<String> response =
                    kafkaRequest.postForEntity(
                            jsonToSend,
                            kafkaServiceUrl,
                            kafkaRequest.RETRIEVE_AND_PRODUCE_MESSAGES_HEADER);

            logger.info(response.getStatusCode().toString());
            logger.info("Message to Kafka Topic has been sent [CardComposerService To ImageHandlerService]");
        } catch (Exception e) {
            logger.error("Have got a error while trying to produce a message to KafkaTopic [CardComposerService To ImageHandlerService]", e);
        }
    }
}
