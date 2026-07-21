package com.kelompok3.order_service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.kelompok3.order_service.dto.OrderEvent;

@Component
public class OrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    @RabbitListener(queues = "order.queue", containerFactory = "rabbitListenerContainerFactory")
    public void consumeOrderEvent(OrderEvent event) {

        log.info("Menerima pesanan: {}", event.getOrderId());
        log.info("Detail pesanan diterima -> produk: {}, qty: {}, status: {}",
                event.getProductName(), event.getQuantity(), event.getStatus());

        try {
            processOrder(event);
            log.info("Order id={} berhasil diproses.", event.getOrderId());
        } catch (Exception e) {
            log.error("Gagal memproses order id={}: {}", event.getOrderId(), e.getMessage());
            throw e;
        }
    }

    private void processOrder(OrderEvent event) {
        log.info("Memproses pembaruan stok untuk produk '{}' sebanyak {} unit...",
                event.getProductName(), event.getQuantity());

        // Simulasi kegagalan untuk menguji Retry & Dead Letter Queue (DLQ)
        if (event.getQuantity() == null || event.getQuantity() <= 0) {
            throw new RuntimeException("Quantity tidak valid, proses gagal!");
        }
    }
}
