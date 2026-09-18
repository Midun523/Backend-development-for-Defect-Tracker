package com.defecttracker.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sub_module_dev_allocations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubModuleDevAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_module_id", nullable = false)
    private SubModule subModule;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime assignedDate;

    @Transient
    @JsonProperty("employeeId")
    public Long getEmployeeId() {
        return employee != null ? employee.getId() : null;
    }

    @Transient
    @JsonProperty("employeeName")
    public String getEmployeeName() {
        if (employee != null) {
            String f = employee.getFirstName() != null ? employee.getFirstName() : "";
            String l = employee.getLastName() != null ? employee.getLastName() : "";
            return (f + " " + l).trim();
        }
        return "Developer";
    }

    @Transient
    @JsonProperty("submoduleId")
    public Long getSubmoduleId() {
        return subModule != null ? subModule.getId() : null;
    }
}

