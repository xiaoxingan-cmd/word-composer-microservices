package com.shayakum.CardComposerService.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shayakum.CardComposerService.utils.KafkaRequest;
import com.shayakum.CardComposerService.utils.ReceivedRecord;
import com.shayakum.CardComposerService.utils.patterns.obs.Observer;
import com.shayakum.CardComposerService.utils.patterns.obs.Subject;
import com.shayakum.CardComposerService.utils.resources.YamlPropertySourceFactory;
import jakarta.annotation.PostConstruct;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@PropertySource(value = "classpath:auth.yml", factory = YamlPropertySourceFactory.class)
public class PrepareConsumerService implements Subject {
    @Value("${kafkaConsumer.url}")
    private String kafkaUrl;
    @Value("${kafkaConsumer.topic}")
    private String kafkaTopic;
    private String kafkaServiceUrl;

    private final KafkaRequest kafkaRequest;
    private final Logger logger = LoggerFactory.getLogger(PrepareConsumerService.class);
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public PrepareConsumerService(KafkaRequest kafkaRequest) {
        this.kafkaRequest = kafkaRequest;
    }

    @PostConstruct
    public void init() {
        if (kafkaUrl == null || kafkaUrl.isEmpty()) {
            throw new IllegalStateException("kafkaUrl is not configured properly.");
        }
        kafkaServiceUrl = kafkaUrl + "/consumers/instance-3-group/instances/instance-3";
    }

    private List<Observer> observers = new ArrayList<>();
    private String latestNews;

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(latestNews);
        }
    }

    public void setLatestNews(String news) {
        this.latestNews = news;
        notifyObservers();
    }

    public void registerConsumer() throws JsonProcessingException, InterruptedException, HttpException {
        try {
            logger.info("Creating a Kafka Consumer and a consumer group and starting to register it....");
            // Create consumer-group and consumer
            Map<String, Object> jsonToSend = new HashMap<>();
            jsonToSend.put("name", "instance-3");
            jsonToSend.put("auto.offset.reset", "earliest");
            jsonToSend.put("format", "json");
            jsonToSend.put("enable.auto.commit", false);
            jsonToSend.put("fetch.min.bytes", 512);
            jsonToSend.put("consumer.request.timeout.ms", 60000);

            try {
                ResponseEntity<String> response =
                        kafkaRequest.postForEntity(
                                jsonToSend,
                                kafkaUrl + "/consumers/instance-3-group",
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

        } catch (HttpClientErrorException e) {
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

    private void retrieveAndProceedMessages(String base_url) throws JsonProcessingException, InterruptedException, HttpException {
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
                    setLatestNews(node.get("value").asText());
                    commitOffset(new ReceivedRecord(node.get("value").asText(), node.get("partition").asInt(), node.get("offset").asLong()), kafkaTopic, base_url);
                }
            }
        }
    }

    private HttpStatusCode commitOffset(ReceivedRecord record, String fromTopic, String base_url) throws HttpException {
        Map<String, Object> offsetData = new HashMap<>();
        offsetData.put("topic", fromTopic);
        offsetData.put("partition", record.getPartition());
        offsetData.put("offset", record.getOffset());

        Map<String, Object> jsonToSend = Map.of(
                "offsets", List.of(offsetData)
        );

        try {
            ResponseEntity<String> response =
                    kafkaRequest.postForEntity(
                            jsonToSend,
                            base_url + "/offsets",
                            kafkaRequest.CONFIGURE_AND_SUBSCRIBE_HEADER);

            return response.getStatusCode();
        } catch (Exception e) {
            logger.error("Have got an error during committing an offset: " + record + ", " + fromTopic, e);
            throw new HttpException();
        }
    }
}
