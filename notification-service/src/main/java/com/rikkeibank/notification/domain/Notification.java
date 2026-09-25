package com.rikkeibank.notification.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sagaId;
    private String type;          // TRANSFER_COMPLETED / TRANSFER_FAILED
    @Column(length = 500)
    private String content;
    private LocalDateTime sentAt = LocalDateTime.now();

    public Long getId() { return id; }
    public String getSagaId() { return sagaId; }
    public void setSagaId(String v) { this.sagaId = v; }
    public String getType() { return type; }
    public void setType(String v) { this.type = v; }
    public String getContent() { return content; }
    public void setContent(String v) { this.content = v; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime v) { this.sentAt = v; }
}
