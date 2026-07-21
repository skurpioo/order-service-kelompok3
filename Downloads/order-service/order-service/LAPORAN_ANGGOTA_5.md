# LAPORAN TUGAS RANCANG WEB SERVICE (TC716D)
## TOPIK 2 – RABBITMQ & ORDER (KELOMPOK 3 & 4)

**Tugas Spesifik**: Anggota 5 (Retry, DLQ & Dokumen)  
**Teknologi**: Java 21, Spring Boot 3.5.3, Spring AMQP, RabbitMQ Server, PostgreSQL  

---

## 1. PENDAHULUAN

### 1.1 Latar Belakang
Dalam arsitektur microservices berorientasi pesan (*message-driven architecture*), keandalan pengiriman dan pemrosesan pesan merupakan aspek krusial. Pada pemrosesan pesanan (order processing), kegagalan sementara (*transient error*) seperti jaringan lambat atau database sibuk tidak boleh menyebabkan pesan hilang begitu saja. 

Oleh karena itu, diperlukan sistem yang mampu melakukan percobaan ulang otomatis (*retry mechanism*) serta memindahkan pesan yang mengalami kegagalan permanen ke antrean khusus yang disebut **Dead Letter Queue (DLQ)** untuk penanganan lebih lanjut tanpa menghentikan seluruh sistem.

### 1.2 Tujuan
Sebagai **Anggota 5**, tujuan utama implementasi ini adalah:
1. Mengimplementasikan mekanisme **Retry otomatis sebanyak 3x** dengan jeda (*exponential backoff*).
2. Mengonfigurasi **Dead Letter Queue (DLQ)** dan **Dead Letter Exchange (DLX)** untuk menampung pesan yang gagal diproses setelah 3x percobaan.
3. Menyediakan listener khusus (`DeadLetterConsumer`) untuk menangani dan mencatat pesan yang masuk ke DLQ.
4. Menyusun dokumentasi pengujian dan bukti eksekusi sistem.

---

## 2. SPESIFIKASI IMPLEMENTASI KODE

### 2.1 Konfigurasi Retry & Listener Factory (`RabbitMQConfig.java`)
Konfigurasi `SimpleRabbitListenerContainerFactory` dilengkapi dengan `AdviceChain` menggunakan `RetryInterceptorBuilder.stateless()`:
- **Max Attempts**: 3 kali percobaan (1 percobaan utama + 2 kali retry).
- **Backoff Options**: Initial interval 2000 ms (2 detik), multiplier 2.0, max interval 10000 ms.
- **Recoverer**: `RepublishMessageRecoverer` yang secara otomatis mempublikasikan ulang pesan yang gagal ke `order.dlx` dengan routing key `order.dlq.routing.key`.

```java
@Bean
public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory,
        Jackson2JsonMessageConverter converter,
        RabbitTemplate rabbitTemplate) {

    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(converter);
    factory.setAdviceChain(retryInterceptor(rabbitTemplate));
    return factory;
}

private Advice retryInterceptor(RabbitTemplate rabbitTemplate) {
    return RetryInterceptorBuilder.stateless()
            .maxAttempts(3)
            .backOffOptions(2000, 2.0, 10000)
            .recoverer(new RepublishMessageRecoverer(
                    rabbitTemplate,
                    DeadLetterConfig.DLQ_EXCHANGE,
                    DeadLetterConfig.DLQ_ROUTING_KEY))
            .build();
}
```

### 2.2 Konfigurasi Dead Letter Queue & Exchange (`DeadLetterConfig.java`)
Mendefinisikan komponen pendukung DLQ di RabbitMQ:
- **Queue DLQ**: `order.dlq` (Durable)
- **Exchange DLX**: `order.dlx` (Direct Exchange)
- **Routing Key DLQ**: `order.dlq.routing.key`

```java
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
        return BindingBuilder.bind(orderDlq).to(orderDlxExchange).with(DLQ_ROUTING_KEY);
    }
}
```

### 2.3 Order Consumer dengan Simulasi Failure (`OrderConsumer.java`)
Consumer utama yang mendengarkan `order.queue`. Jika `quantity <= 0` atau `null`, melempar `RuntimeException` untuk memicu siklus Retry dan DLQ.

```java
@Component
public class OrderConsumer {
    private static final Logger log = LoggerFactory.getLogger(OrderConsumer.class);

    @RabbitListener(queues = "order.queue", containerFactory = "rabbitListenerContainerFactory")
    public void consumeOrderEvent(OrderEvent event) {
        log.info("Menerima pesanan: {}", event.getOrderId());
        try {
            processOrder(event);
            log.info("Order id={} berhasil diproses.", event.getOrderId());
        } catch (Exception e) {
            log.error("Gagal memproses order id={}: {}", event.getOrderId(), e.getMessage());
            throw e; // Mandatory throw exception agar retry & DLQ terpicu
        }
    }

    private void processOrder(OrderEvent event) {
        if (event.getQuantity() == null || event.getQuantity() <= 0) {
            throw new RuntimeException("Quantity tidak valid, proses gagal!");
        }
    }
}
```

### 2.4 Dead Letter Consumer (`DeadLetterConsumer.java`)
Listener otomatis pada `order.dlq` untuk menangkap pesan yang diekskalasi.

```java
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
```

---

## 3. HASIL PENGUJIEN & ANALISIS LOG

### 3.1 Pengujian 1: Verifikasi Consumer Aktif
Eksekusi CLI RabbitMQ:
```powershell
.\rabbitmqctl.bat list_consumers
```
**Hasil**:
Terdaftar 2 Consumer terhubung pada vhost `/`:
1. Consumer terikat pada queue `order.queue` (Tag: `amq.ctag-YQzx7zMnAEU87j2EIz8EIg`).
2. Consumer terikat pada queue `order.dlq` (Tag: `amq.ctag-LrzjX9JoIOYjlMN0ykTMaw`).

---

### 3.2 Pengujian 2: Skenario Retry 3x dan Dead Letter Queue (DLQ)
Dikirimkan payload uji berisi pesan ber-`quantity` = 0:
`OrderEvent{orderId=99, productName='Produk Error Test DLQ', quantity=0, status='TEST'}`

**Kronologi & Analisis Log**:

1. **Percobaan Ke-1 (Initial Attempt) - 04:06:32**:
   ```text
   2026-07-22T04:06:32.873+07:00 INFO  OrderConsumer : Menerima pesanan: 99
   2026-07-22T04:06:32.874+07:00 ERROR OrderConsumer : Gagal memproses order id=99: Quantity tidak valid, proses gagal!
   ```
   *Penjelasan*: Message pertama kali diterima, validasi gagal, exception dilempar ke container.

2. **Percobaan Ke-2 (Retry 1) - 04:06:34 (Jeda 2 Detik)**:
   ```text
   2026-07-22T04:06:34.879+07:00 INFO  OrderConsumer : Menerima pesanan: 99
   2026-07-22T04:06:34.892+07:00 ERROR OrderConsumer : Gagal memproses order id=99: Quantity tidak valid, proses gagal!
   ```
   *Penjelasan*: Backoff interseptor menahan 2 detik (2000 ms), lalu melakukan retry ke-1.

3. **Percobaan Ke-3 (Retry 2) - 04:06:38 (Jeda 4 Detik)**:
   ```text
   2026-07-22T04:06:38.931+07:00 INFO  OrderConsumer : Menerima pesanan: 99
   2026-07-22T04:06:38.932+07:00 ERROR OrderConsumer : Gagal memproses order id=99: Quantity tidak valid, proses gagal!
   ```
   *Penjelasan*: Backoff multiplier (2.0) memperpanjang jeda menjadi 4 detik (4000 ms), lalu retry ke-2 dijalankan. Total percobaan telah mencapai 3 kali (batas maksimum).

4. **Eskalasi ke Dead Letter Exchange (DLX)**:
   ```text
   2026-07-22T04:06:38.938+07:00 WARN  RepublishMessageRecoverer : Republishing failed message to exchange 'order.dlx' with routing key order.dlq.routing.key
   ```
   *Penjelasan*: Karena batas percobaan 3x terlampaui, `RepublishMessageRecoverer` mengambil alih dan mempublikasikan pesan ke `order.dlx`.

5. **Pesan Diterima oleh DeadLetterConsumer (DLQ)**:
   ```text
   2026-07-22T04:06:39.023+07:00 WARN  DeadLetterConsumer : =================================================
   2026-07-22T04:06:39.033+07:00 WARN  DeadLetterConsumer : !!! Pesan masuk Dead Letter Queue (order.dlq): ...
   ```
   *Penjelasan*: Message berhasil sampai ke `order.dlq` dan langsung ditangani oleh `DeadLetterConsumer`.

---

## 4. KESIMPULAN

Berdasarkan seluruh proses rancangan, konfigurasi, dan eksekusi pengujian yang telah dilakukan:
1. Mekanisme **Retry 3x** berfungsi secara presisi sesuai urutan interval waktu backoff.
2. Mekanisme **Dead Letter Queue (DLQ)** berhasil diisolasi sehingga pesan bermasalah tidak hilang (*fault tolerance*) dan tidak menyumbat queue utama.
3. Seluruh detail implementasi tugas **Anggota 5** pada **Topik 2 (RabbitMQ & Order)** telah terpenuhi 100%.
