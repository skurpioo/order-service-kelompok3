package com.kelompok3.order_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kelompok3.order_service.model.Order;
import com.kelompok3.order_service.repository.OrderRepository;

@Service // nandain class sebagai service layer
public class OrderService {

    @Autowired // inject repository secara otomatis
    private OrderRepository orderRepository;

    // simpan order baru
    public Order createOrder(Order order) {
        return orderRepository.save(order);
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