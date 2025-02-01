package com.shayakum.CardRepresentorService.services;

import com.shayakum.CardRepresentorService.utils.KafkaRequest;
import com.shayakum.CardRepresentorService.utils.enums.ObjectStatus;
import com.shayakum.CardRepresentorService.utils.resources.YamlPropertySourceFactory;
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
    public String kafkaTopic;

    public void produceMessage(String kafkaTopic, String nameOfWord, ObjectStatus objectStatus, String id) {
        Map<String, Object> jsonToSend = Map.of(
                "records", List.of(Map.of("value", nameOfWord.toUpperCase() + "::" + objectStatus + "::" + id))
        );

        try {
            ResponseEntity<String> response =
                    kafkaRequest.postForEntity(
                            jsonToSend,
                            kafkaUrl + kafkaTopic,
                            kafkaRequest.RETRIEVE_AND_PRODUCE_MESSAGES_HEADER);

            logger.info("Message to Kafka Topic has been sent [From CardRepresentorService To InitialService]");
        } catch (Exception e) {
            logger.error("Have got an error during sending a message to Kafka Topic [From CardRepresentorService To InitialService]", e);
        }
    }
}
