package com.kelompok3.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kelompok3.order_service.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}