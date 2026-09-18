package com.defecttracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sub_modules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "module_id", nullable = false)
    @JsonIgnore
    private Module module;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("subModuleName")
    public String fetchSubModuleName() {
        return name;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("submoduleName")
    public String fetchSubmoduleName() {
        return name;
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("moduleId")
    public Long fetchModuleId() {
        return module != null ? module.getId() : null;
    }
}
