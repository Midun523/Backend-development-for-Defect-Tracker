package com.defecttracker.service.impl;

import com.defecttracker.service.WhatsAppBridgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class WhatsAppBridgeServiceBridgeImpl implements WhatsAppBridgeService {

    @Value("${app.whatsapp.api-url:http://10.197.82.72:3000}")
    private String whatsappApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean sendMessage(String phoneNumber, String message) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty() || message == null) {
            return false;
        }

        try {
            String endpoint = whatsappApiUrl + "/send-message";
            Map<String, String> body = new HashMap<>();
            body.put("phone", phoneNumber);
            body.put("message", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(endpoint, request, String.class);
            log.info("WhatsApp message sent to {}: status {}", phoneNumber, response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            log.warn("Could not reach WhatsApp server at {}: {}", whatsappApiUrl, ex.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("apiUrl", whatsappApiUrl);
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(whatsappApiUrl + "/status", Map.class);
            status.put("connected", response.getStatusCode().is2xxSuccessful());
            status.put("response", response.getBody());
        } catch (Exception ex) {
            status.put("connected", false);
            status.put("error", ex.getMessage());
        }
        return status;
    }

    @Override
    public void processWebhook(Map<String, Object> payload) {
        log.info("Received WhatsApp Webhook: {}", payload);
    }
}
