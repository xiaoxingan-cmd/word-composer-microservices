package com.shayakum.CardComposerService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shayakum.CardComposerService.services.PrepareConsumerService;
import org.apache.http.HttpException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableDiscoveryClient
public class CardComposerServiceApplication {
	private final PrepareConsumerService prepareConsumerService;

	@Autowired
	public CardComposerServiceApplication(PrepareConsumerService prepareConsumerService) {
		this.prepareConsumerService = prepareConsumerService;
	}

	public static void main(String[] args) {
		SpringApplication.run(CardComposerServiceApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}

	@EventListener(ContextRefreshedEvent.class)
	public void startKafkaConsumer() throws HttpException, JsonProcessingException, InterruptedException {
		prepareConsumerService.registerConsumer();
	}
}
