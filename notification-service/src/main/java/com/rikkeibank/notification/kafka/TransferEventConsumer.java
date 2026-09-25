package com.rikkeibank.notification.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikkeibank.notification.domain.Notification;
import com.rikkeibank.notification.domain.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * CONSUMER Kafka: nghe sự kiện chuyển khoản từ transaction-service (event-driven,
 * bất đồng bộ, loose coupling) và tạo thông báo biến động số dư.
 */
@Component
public class TransferEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferEventConsumer.class);

    private final NotificationRepository repo;
    private final ObjectMapper mapper;

    public TransferEventConsumer(NotificationRepository repo, ObjectMapper mapper) {
        this.repo = repo; this.mapper = mapper;
    }

    @KafkaListener(topics = "transaction-events", groupId = "notification-service")
    public void onEvent(String message) {
        try {
            JsonNode ev = mapper.readTree(message);
            String status = ev.path("status").asText();
            String sagaId = ev.path("sagaId").asText();

            Notification n = new Notification();
            n.setSagaId(sagaId);
            if ("COMPLETED".equals(status)) {
                n.setType("TRANSFER_COMPLETED");
                n.setContent(String.format("Chuyển khoản thành công %s -> %s: %s",
                        ev.path("fromAcc").asText(), ev.path("toAcc").asText(), ev.path("amount").asText()));
            } else {
                n.setType("TRANSFER_FAILED");
                n.setContent(String.format("Chuyển khoản THẤT BẠI %s -> %s: %s (đã hoàn tiền nếu cần)",
                        ev.path("fromAcc").asText(), ev.path("toAcc").asText(), ev.path("amount").asText()));
            }
            repo.save(n);
            log.info("[NOTIFY] {} | {}", n.getType(), n.getContent());
        } catch (Exception e) {
            log.error("Không xử lý được event: {} ({})", message, e.getMessage());
        }
    }
}
