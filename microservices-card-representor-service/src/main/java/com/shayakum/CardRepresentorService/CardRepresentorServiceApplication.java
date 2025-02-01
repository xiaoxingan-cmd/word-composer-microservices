package com.shayakum.CardRepresentorService;

import com.shayakum.CardRepresentorService.services.PrepareConsumerService;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.io.IOException;

@SpringBootApplication
@EnableDiscoveryClient
public class CardRepresentorServiceApplication {
	private final PrepareConsumerService prepareConsumerService;

	@Autowired
	public CardRepresentorServiceApplication(PrepareConsumerService prepareConsumerService) {
		this.prepareConsumerService = prepareConsumerService;
	}

	public static void main(String[] args) {
		System.setProperty("aws.java.v1.disableDeprecationAnnouncement", "true");
		SpringApplication.run(CardRepresentorServiceApplication.class, args);
	}

	@EventListener(ContextRefreshedEvent.class)
	public void startKafkaConsumer() throws IOException, InterruptedException, HttpException {
		prepareConsumerService.registerConsumer();
	}
}
