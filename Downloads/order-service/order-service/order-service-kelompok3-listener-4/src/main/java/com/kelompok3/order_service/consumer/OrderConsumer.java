package com.kelompok3.order_service.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.kelompok3.order_service.dto.OrderEvent;

@Component
public class OrderConsumer {

    // Logger dipakai supaya kita bisa lihat bukti pesan diterima di console IDE.
    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    @RabbitListener(queues = "order.queue")
    public void consumeOrderEvent(OrderEvent event) {

        log.info("Menerima pesanan: {}", event.getOrderId());
        log.info("Detail pesanan diterima -> produk: {}, qty: {}, status: {}",
                event.getProductName(), event.getQuantity(), event.getStatus());

        try {
            // ==========================================================
            // SIMULASI LOGIKA BISNIS.
            // Di project nyata, di sinilah kita misalnya:
            //  - mengurangi stok produk di database
            //  - memproses pembayaran
            //  - mengirim notifikasi ke user
            // Untuk tugas kuliah ini, cukup simulasi sederhana seperti
            // di bawah, sesuai instruksi outline Anggota 4.
            // ==========================================================
            processOrder(event);

            log.info("Order id={} berhasil diproses.", event.getOrderId());

        } catch (Exception e) {
            // Exception di-throw ulang (bukan ditelan / di-catch diam-diam)
            // supaya mekanisme RETRY & DEAD LETTER QUEUE milik Anggota 5
            // bisa jalan. Kalau exception ditelan di sini, Spring akan
            // menganggap pesan SUKSES diproses, padahal gagal -- retry
            // dan DLQ jadi tidak pernah terpicu.
            log.error("Gagal memproses order id={}: {}", event.getOrderId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Simulasi proses bisnis sederhana: update stok / pembayaran.
     * Silakan dikembangkan sesuai kebutuhan tugas (misalnya baca dari
     * tabel stok, panggil service lain, dst).
     */
    private void processOrder(OrderEvent event) {
        log.info("Memproses pembaruan stok untuk produk '{}' sebanyak {} unit...",
                event.getProductName(), event.getQuantity());

        // Simulasi delay proses (opsional, boleh dihapus)
        // Contoh titik untuk simulasi kegagalan (dipakai Anggota 5 saat
        // menguji Retry & DLQ), boleh diaktifkan sementara untuk testing:
        //
        // if (event.getQuantity() == null || event.getQuantity() <= 0) {
        //     throw new RuntimeException("Quantity tidak valid, proses gagal!");
        // }
    }
}