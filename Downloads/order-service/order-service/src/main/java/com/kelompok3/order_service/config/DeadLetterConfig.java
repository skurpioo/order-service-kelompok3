package com.kelompok3.order_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeadLetterConfig {

    public static final String DLQ_QUEUE = "order.dlq";
    public static final String DLQ_EXCHANGE = "order.dlx";
    public static final String DLQ_ROUTING_KEY = "order.dlq.routing.key";

    @Bean
    public Queue orderDlq() {
        return new Queue(DLQ_QUEUE, true);
    }

    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(DLQ_EXCHANGE);
    }

    @Bean
    public Binding orderDlqBinding(Queue orderDlq, DirectExchange orderDlxExchange) {
        return BindingBuilder
                .bind(orderDlq)
                .to(orderDlxExchange)
                .with(DLQ_ROUTING_KEY);
    }
}