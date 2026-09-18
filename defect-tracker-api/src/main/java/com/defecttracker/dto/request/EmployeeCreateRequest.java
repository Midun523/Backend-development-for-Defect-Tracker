package com.defecttracker.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    private String phone;
    private String gender; // Male, Female, Other
    private Long designationId;
    private Double experience;
    private LocalDate joinedDate;
    private List<String> skills;
    private Integer availability; // 0-100
    private String status; // active, inactive, on-leave
    private String department;
    private String manager;
    private LocalDate startDate;
    private LocalDate endDate;
    private String address;
    private Long roleId;
    private String password;
}
