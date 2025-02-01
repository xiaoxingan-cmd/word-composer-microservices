package com.shayakum.ImageHandlerService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flickr4java.flickr.FlickrException;
import com.shayakum.ImageHandlerService.services.PrepareConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableDiscoveryClient
public class ImageHandlerServiceApplication {
	private final PrepareConsumerService prepareConsumerService;

	@Autowired
	public ImageHandlerServiceApplication(PrepareConsumerService prepareConsumerService) {
		this.prepareConsumerService = prepareConsumerService;
	}

	public static void main(String[] args) {
		System.setProperty("aws.java.v1.disableDeprecationAnnouncement", "true");
		SpringApplication.run(ImageHandlerServiceApplication.class, args);
	}

	@EventListener(ContextRefreshedEvent.class)
	public void startKafkaConsumer() throws JsonProcessingException, InterruptedException, FlickrException {
		prepareConsumerService.registerConsumer();
	}
}
