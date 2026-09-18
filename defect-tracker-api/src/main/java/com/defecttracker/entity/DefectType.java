package com.defecttracker.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "defect_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefectType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; // UI, Functionality, Performance, Security, Backend/API, Database, Logic

    @Column(length = 255)
    private String description;

    @JsonProperty("defectTypeName")
    public String getDefectTypeName() {
        return name;
    }

    @JsonProperty("defectTypeName")
    public void setDefectTypeName(String defectTypeName) {
        if (this.name == null || this.name.isEmpty()) {
            this.name = defectTypeName;
        }
    }
}
