package com.defecttracker.service.impl;

import com.defecttracker.service.EmailService;
import com.defecttracker.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;

    @Override
    public void notify(String eventType, String recipientEmail, Map<String, String> variables) {
        log.info("Dispatching notification event: {} to recipient: {}", eventType, recipientEmail);
        try {
            emailService.sendTemplatedEmail(recipientEmail, eventType, variables);
        } catch (Exception ex) {
            log.warn("Failed to dispatch email notification {}: {}", eventType, ex.getMessage());
        }
    }
}
