package com.rikkeibank.transaction.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikkeibank.transaction.domain.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Phát sự kiện lên Kafka (bất đồng bộ, event-driven) cho notification-service tiêu thụ. */
@Component
public class TransferEventPublisher {

    public static final String TOPIC = "transaction-events";
    private static final Logger log = LoggerFactory.getLogger(TransferEventPublisher.class);

    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;

    public TransferEventPublisher(KafkaTemplate<String, String> kafka, ObjectMapper mapper) {
        this.kafka = kafka; this.mapper = mapper;
    }

    public void publishCompleted(Transaction tx) { publish(tx, "COMPLETED"); }
    public void publishFailed(Transaction tx) { publish(tx, "FAILED"); }

    private void publish(Transaction tx, String status) {
        try {
            TransferEvent ev = new TransferEvent(
                    tx.getSagaId(), tx.getId(), tx.getFromAcc(), tx.getToAcc(), tx.getAmount(), status);
            kafka.send(TOPIC, tx.getSagaId(), mapper.writeValueAsString(ev));
        } catch (Exception e) {
            log.error("Không phát được event cho saga {}: {}", tx.getSagaId(), e.getMessage());
        }
    }
}
