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
public class ClientRequest {
    @NotBlank(message = "Client name is required")
    private String clientName;
    private String phoneNumber;
    private String email;
    private String country;
    private String state;
}
