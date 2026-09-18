package com.defecttracker.service;

import java.util.Map;

public interface WhatsAppBridgeService {
    boolean sendMessage(String phoneNumber, String message);
    Map<String, Object> getStatus();
    void processWebhook(Map<String, Object> payload);
}
