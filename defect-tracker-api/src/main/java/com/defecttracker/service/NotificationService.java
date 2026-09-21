package com.defecttracker.service;

import java.util.Map;

public interface NotificationService {

    void notify(String eventType, String recipientEmail, Map<String, String> variables);
}
