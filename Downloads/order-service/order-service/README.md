# Order Service - Topik 2: RabbitMQ & Order (Kelompok 3 & 4)

Aplikasi Microservice **Order Service** berbasis Spring Boot 3, PostgreSQL, dan RabbitMQ yang mengimplementasikan pesan asinkron, pola konsumen, mekanisme **Retry 3x**, serta **Dead Letter Queue (DLQ)**.

---

## 👥 Pembagian Tugas Kelompok

| No. Anggota | Tugas Utama | Detail Implementasi | Status |
| :--- | :--- | :--- | :---: |
| **Anggota 1** | CRUD Order (PostgreSQL) | Endpoint `POST /orders`, `GET /orders`, `GET /orders/{id}`, `PUT /orders/{id}`, `DELETE /orders/{id}` | ✅ Selesai |
| **Anggota 2** | Koneksi RabbitMQ | Setup koneksi, queue (`order.queue`), exchange (`order.exchange`), dan binding | ✅ Selesai |
| **Anggota 3** | Producer | Pengiriman event `OrderEvent` ke RabbitMQ Exchange saat order dibuat | ✅ Selesai |
| **Anggota 4** | Consumer (Choreography) | Listener otomatis (`OrderConsumer`) untuk memproses pesan dari `order.queue` | ✅ Selesai |
| **Anggota 5** | Retry, DLQ & Dokumen | Config Retry 3x, setup Dead Letter Queue (`order.dlq` & `order.dlx`), penulisan README & bukti pengujian | ✅ Selesai |

---

## 🏗️ Arsitektur RabbitMQ & DLQ (Anggota 5)

```
[ Producer ] --(order.routing.key)--> [ order.exchange (Topic) ]
                                              |
                                              v
                                       [ order.queue ]
                                              |
                                       (OrderConsumer)
                                              |
                                    (Gagal 3x / Exception)
                                              |
                                              v
                                   [ order.dlx (Direct) ]
                                              |
                                   (order.dlq.routing.key)
                                              |
                                              v
                                        [ order.dlq ]
                                              |
                                    (DeadLetterConsumer)
```

---

## 🚀 Cara Menjalankan Aplikasi

### 1. Prerequisites
- **Java**: Version 21
- **PostgreSQL**: Port `5433`, Database `order_db`, Username `postgres`, Password `123`
- **RabbitMQ**: Port `5672` (AMQP) & `15672` (Management UI), Credentials `guest`/`guest`

### 2. Run Application
Jalankan perintah berikut di root folder project:
```powershell
.\mvnw.cmd clean spring-boot:run
```

---

## 🧪 Panduan Pengujian & Screenshot (Dokumentasi Anggota 5)

### 1. Verifikasi Consumer Aktif di RabbitMQ
Jalankan perintah di Command Prompt / PowerShell:
```powershell
cd "C:\Program Files\RabbitMQ Server\rabbitmq_server-4.3.2\sbin"
.\rabbitmqctl.bat list_consumers
```
> **Screenshot 1**: Menunjukkan 2 consumer aktif terdaftar pada queue `order.queue` dan `order.dlq`.

---

### 2. Pengujian Retry 3x & Dead Letter Queue (DLQ)

#### Case A: Pengujian Sukses (Normal Order)
- Di `OrderServiceApplication.java`:
  ```java
  OrderEvent testEvent = new OrderEvent(1L, "Laptop Gaming", 1, "SUCCESS");
  ```
- **Log Hasil**:
  ```text
  INFO  c.k.o.consumer.OrderConsumer : Menerima pesanan: 1
  INFO  c.k.o.consumer.OrderConsumer : Memproses pembaruan stok untuk produk 'Laptop Gaming' sebanyak 1 unit...
  INFO  c.k.o.consumer.OrderConsumer : Order id=1 berhasil diproses.
  ```

#### Case B: Pengujian Retry 3x & Eskalasi ke DLQ (Error Order)
- Di `OrderServiceApplication.java` set `quantity` = `0`:
  ```java
  OrderEvent testEvent = new OrderEvent(99L, "Produk Error Test DLQ", 0, "TEST");
  ```
- **Log Hasil (Retry 3x -> DLQ)**:
  ```text
  // Attempt 1
  ERROR c.k.o.consumer.OrderConsumer : Gagal memproses order id=99: Quantity tidak valid, proses gagal!
  
  // Attempt 2 (Retry ke-1 setelah 2 detik)
  ERROR c.k.o.consumer.OrderConsumer : Gagal memproses order id=99: Quantity tidak valid, proses gagal!
  
  // Attempt 3 (Retry ke-2 setelah 4 detik)
  ERROR c.k.o.consumer.OrderConsumer : Gagal memproses order id=99: Quantity tidak valid, proses gagal!
  
  // Republish ke DLX
  WARN  o.s.a.r.retry.RepublishMessageRecoverer : Republishing failed message to exchange 'order.dlx' with routing key order.dlq.routing.key
  
  // Ditangkap DeadLetterConsumer
  WARN  c.k.o.consumer.DeadLetterConsumer : !!! Pesan masuk Dead Letter Queue (order.dlq): ...
  ```
> **Screenshot 2**: Ambil screenshot log terminal VS Code yang menampilkan siklus 3x retry dan log penangkapan pesan oleh `DeadLetterConsumer`.

---

## 🛠️ Struktur Project

```text
src/main/java/com/kelompok3/order_service/
├── OrderServiceApplication.java
├── config/
│   ├── DeadLetterConfig.java     <-- Config Queue order.dlq & Exchange order.dlx (Anggota 5)
│   └── RabbitMQConfig.java       <-- Config RetryInterceptor 3x & Backoff (Anggota 5)
├── consumer/
│   ├── DeadLetterConsumer.java   <-- Consumer Listener order.dlq (Anggota 5)
│   └── OrderConsumer.java       <-- Consumer Listener order.queue (Anggota 4)
├── controller/
│   └── OrderController.java      <-- CRUD REST API (Anggota 1)
├── dto/
│   └── OrderEvent.java          <-- DTO Payload RabbitMQ
├── model/
│   └── Order.java                <-- Entity JPA (Anggota 1)
├── producer/
│   └── OrderProducer.java       <-- RabbitMQ Producer (Anggota 3)
├── repository/
│   └── OrderRepository.java     <-- JPA Repository (Anggota 1)
└── service/
    └── OrderService.java         <-- Business Logic Order (Anggota 1)
```
