package com.kelompok3.order_service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.kelompok3.order_service.dto.OrderEvent;

@Component
public class DeadLetterConsumer {

    private static final Logger log = LoggerFactory.getLogger(DeadLetterConsumer.class);

    @RabbitListener(queues = "order.dlq")
    public void handleDeadLetter(Object payload) {
        log.warn("=================================================");
        log.warn("!!! Pesan masuk Dead Letter Queue (order.dlq): {}", payload);
        log.warn("=================================================");
    }
}
