package com.kelompok3.order_service.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kelompok3.order_service.config.RabbitMQConfig;
import com.kelompok3.order_service.dto.OrderEvent;

/**
 * Producer bertugas mengirim OrderEvent ke RabbitMQ Exchange
 * setiap kali ada order baru yang sukses disimpan.
 *
 * Menggunakan RabbitTemplate & konstanta EXCHANGE/ROUTING_KEY
 * yang sudah didefinisikan Anggota 2 di RabbitMQConfig.
 */
@Component
public class OrderProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendOrderEvent(OrderEvent event) {
        log.info("Mengirim event ke RabbitMQ: {}", event);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                event
        );

        log.info("Event order id={} berhasil dikirim ke exchange '{}'",
                event.getOrderId(), RabbitMQConfig.EXCHANGE);
    }
}
