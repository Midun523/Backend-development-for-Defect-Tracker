package com.defecttracker.modules.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {

    private Long id;
    private String employeeId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private Long designationId;
    private String designationName;
    private Long roleId;
    private String roleName;
    private boolean admin;
    private boolean active;
    private BigDecimal initialCapacity;
    private BigDecimal currentCapacity;
    private Set<String> effectivePermissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
