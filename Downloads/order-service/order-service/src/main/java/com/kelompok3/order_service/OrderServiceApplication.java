package com.kelompok3.order_service;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.kelompok3.order_service.config.RabbitMQConfig;
import com.kelompok3.order_service.dto.OrderEvent;

@SpringBootApplication
public class OrderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner testRabbit(RabbitTemplate rabbitTemplate,
			ConnectionFactory connectionFactory) {

		return args -> {
			System.out.println("======================================");
			System.out.println("RabbitMQ Connected Successfully");
			System.out.println(connectionFactory.getClass().getName());

			// --- Option 1: Normal Order Event (Sukses) ---
			OrderEvent testEvent = new OrderEvent(1L, "Laptop Gaming", 1, "SUCCESS");

			// --- Option 2: Testing Retry 3x & DLQ (Error Order - hapus // untuk menguji DLQ) ---
			// OrderEvent testEvent = new OrderEvent(99L, "Produk Error Test DLQ", 0, "TEST");

			rabbitTemplate.convertAndSend(
					RabbitMQConfig.EXCHANGE,
					RabbitMQConfig.ROUTING_KEY,
					testEvent);

			System.out.println("Pesan test (OrderEvent) berhasil dikirim");
			System.out.println("======================================");
		};
	}

}