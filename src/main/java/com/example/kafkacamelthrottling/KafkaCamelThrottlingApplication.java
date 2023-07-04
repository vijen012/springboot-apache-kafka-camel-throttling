package com.example.kafkacamelthrottling;

import com.example.kafkacamelthrottling.config.PublishMessageToCamelRoute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KafkaCamelThrottlingApplication implements CommandLineRunner {


	@Autowired
	private PublishMessageToCamelRoute publishMessageToCamelRoute;

	public static void main(String[] args) {
		SpringApplication.run(KafkaCamelThrottlingApplication.class, args);
	}

	@Override
	public void run(String... args) {
		// Publish a message to the Camel route
		for (int i = 1; i < 50; i++) {
			publishMessageToCamelRoute.sendMessageToCamelRoute("Hello, Apache Camel!" + i);
		}
	}
}
