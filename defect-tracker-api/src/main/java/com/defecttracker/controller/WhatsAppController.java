package com.defecttracker.controller;

import com.defecttracker.dto.response.ApiResponse;
import com.defecttracker.service.WhatsAppBridgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/whatsapp")
@RequiredArgsConstructor
@Tag(name = "WhatsApp Integration Bridge", description = "Endpoints for bridging messages and webhooks with external WhatsApp service")
public class WhatsAppController {

    private final WhatsAppBridgeService whatsAppBridgeService;

    @PostMapping("/send")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Send a WhatsApp notification message")
    public ResponseEntity<ApiResponse<Boolean>> sendMessage(@jakarta.validation.Valid @RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String message = body.get("message");
        boolean sent = whatsAppBridgeService.sendMessage(phone, message);
        return ResponseEntity.ok(ApiResponse.success(sent, sent ? "Message dispatched" : "Failed to dispatch message"));
    }

    @GetMapping("/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get WhatsApp bridge connection status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        Map<String, Object> status = whatsAppBridgeService.getStatus();
        return ResponseEntity.ok(ApiResponse.success(status, "Status retrieved"));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Receive webhook events from WhatsApp server")
    public ResponseEntity<ApiResponse<String>> handleWebhook(@jakarta.validation.Valid @RequestBody Map<String, Object> payload) {
        whatsAppBridgeService.processWebhook(payload);
        return ResponseEntity.ok(ApiResponse.success("Webhook processed", "Event acknowledged"));
    }
}
