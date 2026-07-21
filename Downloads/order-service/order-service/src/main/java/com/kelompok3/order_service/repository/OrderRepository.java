package com.kelompok3.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kelompok3.order_service.model.Order;

// Repository Order — otomatis punya method CRUD dari JpaRepository (Anggota 1: Valentino)
public interface OrderRepository extends JpaRepository<Order, Long> {
}