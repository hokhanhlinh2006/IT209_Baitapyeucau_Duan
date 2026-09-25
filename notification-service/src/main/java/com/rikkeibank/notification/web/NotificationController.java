package com.rikkeibank.notification.web;

import com.rikkeibank.notification.domain.Notification;
import com.rikkeibank.notification.domain.NotificationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Xem lịch sử thông báo đã nhận (minh chứng consumer đã xử lý event). */
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationRepository repo;
    public NotificationController(NotificationRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Notification> all() { return repo.findAll(); }
}
