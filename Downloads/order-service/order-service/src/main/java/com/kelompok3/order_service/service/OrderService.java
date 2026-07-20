package com.kelompok3.order_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kelompok3.order_service.dto.OrderEvent;
import com.kelompok3.order_service.model.Order;
import com.kelompok3.order_service.producer.OrderProducer;
import com.kelompok3.order_service.repository.OrderRepository;

@Service // nandain class sebagai service layer
public class OrderService {

    @Autowired // inject repository secara otomatis
    private OrderRepository orderRepository;

    @Autowired // inject producer buat kirim event ke RabbitMQ
    private OrderProducer orderProducer;

    // simpan order baru
    public Order createOrder(Order order) {
        Order savedOrder = orderRepository.save(order);

        // kirim event ke RabbitMQ setelah order sukses disimpan
        OrderEvent event = new OrderEvent(
                savedOrder.getId(),
                savedOrder.getProductName(),
                savedOrder.getQuantity(),
                savedOrder.getStatus()
        );
        orderProducer.sendOrderEvent(event);

        return savedOrder;
    }

    // ambil semua order
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // ambil order berdasarkan id
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    // update order
    public Order updateOrder(Long id, Order orderDetail) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order tidak ditemukan"));
        order.setProductName(orderDetail.getProductName());
        order.setQuantity(orderDetail.getQuantity());
        order.setPrice(orderDetail.getPrice());
        order.setStatus(orderDetail.getStatus());
        return orderRepository.save(order);
    }

    // hapus order
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}