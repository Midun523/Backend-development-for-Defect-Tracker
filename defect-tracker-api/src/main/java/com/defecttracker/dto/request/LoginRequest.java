package com.defecttracker.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Username or email is required")
    private String username;
    private String email; // optional fallback
    @NotBlank(message = "Password is required")
    private String password;

    public String getEffectiveIdentifier() {
        if (username != null && !username.trim().isEmpty()) {
            return username.trim();
        }
        return email != null ? email.trim() : "";
    }
}
