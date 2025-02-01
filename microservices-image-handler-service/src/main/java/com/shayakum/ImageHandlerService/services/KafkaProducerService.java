package com.shayakum.ImageHandlerService.services;

import com.shayakum.ImageHandlerService.utils.KafkaRequest;
import com.shayakum.ImageHandlerService.utils.enums.ObjectStatus;
import com.shayakum.ImageHandlerService.utils.resources.YamlPropertySourceFactory;
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
    @Value("${kafkaProducer.topicS}")
    public String kafkaTopicSuccess;
    @Value("${kafkaProducer.topicF}")
    public String kafkaTopicFail;
    private String kafkaServiceUrl = "http://127.0.0.1:8008/topics/";

    public void produceMessage(String kafkaTopic, String wordName, ObjectStatus objectStatus , String id) {
        Map<String, Object> jsonToSend = Map.of(
                "records", List.of(Map.of("value", wordName + "::" + objectStatus + "::" + id))
        );

        try {
            ResponseEntity<String> response =
                    kafkaRequest.postForEntity(
                            jsonToSend,
                            kafkaServiceUrl + kafkaTopic,
                            kafkaRequest.RETRIEVE_AND_PRODUCE_MESSAGES_HEADER);

            logger.info("Message to a Kafka Topic has been sent");
        } catch (Exception e) {
            logger.error("Have got an error during sending a message to a Kafka Topic", e);
        }
    }
}
