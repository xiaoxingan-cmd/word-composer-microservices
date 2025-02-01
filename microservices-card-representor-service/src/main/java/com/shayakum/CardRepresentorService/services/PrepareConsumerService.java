package com.shayakum.CardRepresentorService.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shayakum.CardRepresentorService.utils.KafkaRequest;
import com.shayakum.CardRepresentorService.utils.ReceivedRecord;
import com.shayakum.CardRepresentorService.utils.enums.ObjectStatus;
import com.shayakum.CardRepresentorService.utils.resources.YamlPropertySourceFactory;
import jakarta.annotation.PostConstruct;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@PropertySource(value = "classpath:auth.yml", factory = YamlPropertySourceFactory.class)
public class PrepareConsumerService {
    @Value("${kafkaConsumer.url}")
    private String kafkaUrl;
    @Value("${kafkaConsumer.topic}")
    private String kafkaTopic;
    private String kafkaServiceUrl;

    private final KafkaRequest kafkaRequest;
    private final ImageProceederService imageProceederService;
    private final Logger logger = LoggerFactory.getLogger(PrepareConsumerService.class);
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public PrepareConsumerService(KafkaRequest kafkaRequest, ImageProceederService imageProceederService) {
        this.kafkaRequest = kafkaRequest;
        this.imageProceederService = imageProceederService;
    }

    @PostConstruct
    public void init() {
        if (kafkaUrl == null || kafkaUrl.isEmpty()) {
            throw new IllegalStateException("kafkaUrl is not configured properly.");
        }
        kafkaServiceUrl = kafkaUrl + "/consumers/instance-2-group/instances/instance-2";
    }

    public void registerConsumer() throws InterruptedException, IOException, HttpException {
        try {
            logger.info("Creating a Kafka Consumer and a consumer group and starting to register it....");
            // Create consumer-group and consumer
            Map<String, Object> jsonToSend = new HashMap<>();
            jsonToSend.put("name", "instance-2");
            jsonToSend.put("auto.offset.reset", "earliest");
            jsonToSend.put("format", "json");
            jsonToSend.put("enable.auto.commit", false);
            jsonToSend.put("fetch.min.bytes", 512);
            jsonToSend.put("consumer.request.timeout.ms", 60000);

            try {
                ResponseEntity<String> response =
                        kafkaRequest.postForEntity(
                                jsonToSend,
                                kafkaUrl + "/consumers/instance-2-group",
                                kafkaRequest.CONFIGURE_AND_SUBSCRIBE_HEADER);

                String responseBody = response.getBody();
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                kafkaServiceUrl = jsonNode.get("base_uri").asText();

                logger.info("A Kafka Consumer has been registered");
            } catch (Exception e) {
                logger.error("Have got a error during a Kafka Consumer registration", e);
            }

            subscribeToTopic(kafkaServiceUrl);
            retrieveAndProceedMessages(kafkaServiceUrl);

        } catch (HttpClientErrorException | IOException e) {
            logger.warn("A consumer instance with the specified name already exists in the Kafka Bridge or one or more consumer configuration options have invalid values.", e);

            subscribeToTopic(kafkaServiceUrl);
            retrieveAndProceedMessages(kafkaServiceUrl);
        }
    }

    private void subscribeToTopic(String base_url) {
        logger.info("Starting to subscribe to topic...");

        Map<String, Object> jsonToSend = new HashMap<>();
        jsonToSend.put("topics", List.of(kafkaTopic));

        try {
            ResponseEntity<String> response =
                    kafkaRequest.postForEntity(
                            jsonToSend,
                            base_url + "/subscription",
                            kafkaRequest.CONFIGURE_AND_SUBSCRIBE_HEADER);

            logger.info("Consumer is subscribed to the topic");
        } catch (Exception e) {
            logger.error("Consumer hasn't been subscribed to the topic", e);
        }
    }

    private void retrieveAndProceedMessages(String base_url) throws InterruptedException, IOException, HttpException {
        logger.debug("Trying to get messages from Kafka Topic...");
        while (true) {
            logger.debug("Refreshing messages from Topic...");
            String response =
                    kafkaRequest.getForObject(
                            base_url + "/records",
                            kafkaRequest.RETRIEVE_AND_PRODUCE_MESSAGES_HEADER);

            if (!(response.equals("[]"))) {
                logger.info("Have received messages from Kafka Topic");
                JsonNode rootNode = objectMapper.readTree(response);
                for (JsonNode node : rootNode) {
                    String message = node.get("value").asText();

                    String value = message.substring(0, message.indexOf("::"));
                    String status = message.substring(message.indexOf("::") + 2, message.lastIndexOf("::"));
                    String id = message.substring(message.lastIndexOf("::") + 2);

                    if (status.equals(ObjectStatus.SUCCESS.toString())) {
                        imageProceederService.runProceeder(new ReceivedRecord(node.get("value").asText(), node.get("partition").asInt(), node.get("offset").asLong()), base_url, kafkaTopic, value, id);
                    }
                }
            }
        }
    }
}
