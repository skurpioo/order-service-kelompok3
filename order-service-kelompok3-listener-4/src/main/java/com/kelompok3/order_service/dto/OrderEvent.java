package com.kelompok3.order_service.dto;

import java.io.Serializable;

/**
 * DTO yang dikirim sebagai payload event ke RabbitMQ setiap kali
 * ada order baru yang berhasil disimpan ke database.
 *
 * Implements Serializable karena Jackson2JsonMessageConverter (dari
 * konfigurasi Anggota 2) butuh object ini bisa diserialisasi jadi JSON.
 */
public class OrderEvent implements Serializable {

    private Long orderId;
    private String productName;
    private Integer quantity;
    private String status;

    public OrderEvent() {
    }

    public OrderEvent(Long orderId, String productName, Integer quantity, String status) {
        this.orderId = orderId;
        this.productName = productName;
        this.quantity = quantity;
        this.status = status;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "OrderEvent{" +
                "orderId=" + orderId +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", status='" + status + '\'' +
                '}';
    }
}
