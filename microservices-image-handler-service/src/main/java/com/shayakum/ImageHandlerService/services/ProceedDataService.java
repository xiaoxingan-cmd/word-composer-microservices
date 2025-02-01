package com.shayakum.ImageHandlerService.services;

import com.shayakum.ImageHandlerService.utils.KafkaRequest;
import com.shayakum.ImageHandlerService.utils.ReceivedRecord;
import com.shayakum.ImageHandlerService.utils.enums.ObjectStatus;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProceedDataService {
    private final SeekForImageService seekForImageService;
    private final KafkaProducerService kafkaProducerService;
    private final KafkaRequest kafkaRequest;
    private final Logger logger = LoggerFactory.getLogger(ProceedDataService.class);

    @Autowired
    public ProceedDataService(SeekForImageService seekForImageService, KafkaProducerService kafkaProducerService, KafkaRequest kafkaRequest) {
        this.seekForImageService = seekForImageService;
        this.kafkaProducerService = kafkaProducerService;
        this.kafkaRequest = kafkaRequest;
    }

    public void proceedData(ReceivedRecord record, String base_url, String fromTopic) {
        logger.info("Starting to proceed data: " + record.toString());

        String value = record.getValue().substring(0, record.getValue().indexOf("::"));
        String id = record.getValue().substring(record.getValue().indexOf("::") + 2);

        try {
            if (seekForImageService.searchImage(value)) {
                commitOffset(record, fromTopic, base_url);
                kafkaProducerService.produceMessage(kafkaProducerService.kafkaTopicSuccess, value, ObjectStatus.SUCCESS, id);
            } else {
                commitOffset(record, fromTopic, base_url);
                kafkaProducerService.produceMessage(kafkaProducerService.kafkaTopicFail, value, ObjectStatus.FAILED, id);            }
        } catch (Exception e) {
            logger.error("An error occurred during committing or producing message to KafkaTopic");
        }
    }

    private HttpStatusCode commitOffset(ReceivedRecord record, String commitTopic, String base_url) throws HttpException {
        Map<String, Object> offsetData = new HashMap<>();
        offsetData.put("topic", commitTopic);
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
            logger.error("Have got an error during committing an offset: " + record + ", " + commitTopic, e);
            throw new HttpException();
        }
    }
}
