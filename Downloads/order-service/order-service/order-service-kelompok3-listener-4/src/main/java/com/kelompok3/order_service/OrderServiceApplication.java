package com.kelompok3.order_service;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import com.kelompok3.order_service.config.RabbitMQConfig;
import com.kelompok3.order_service.dto.OrderEvent;

@SpringBootApplication
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

	@Bean
	@Profile("!test")
	CommandLineRunner testRabbit(RabbitTemplate rabbitTemplate,
			ConnectionFactory connectionFactory) {

		return args -> {

			System.out.println("======================================");
			System.out.println("RabbitMQ Connected Successfully");
			System.out.println(connectionFactory.getClass().getName());

			OrderEvent testEvent = new OrderEvent(
					0L,
					"Test Koneksi RabbitMQ",
					1,
					"TEST"
			);

			rabbitTemplate.convertAndSend(
					RabbitMQConfig.EXCHANGE,
					RabbitMQConfig.ROUTING_KEY,
					testEvent);

			System.out.println("Pesan berhasil dikirim");
			System.out.println("======================================");
		};
	}

}