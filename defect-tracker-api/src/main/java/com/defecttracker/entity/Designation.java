package com.defecttracker.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "designations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String designationName;

    @Column(length = 500)
    private String description;

    @Builder.Default
    private int totalEmployees = 0;

    @JsonProperty("name")
    public String getName() {
        return designationName;
    }

    @JsonProperty("name")
    public void setName(String name) {
        if (this.designationName == null || this.designationName.isEmpty()) {
            this.designationName = name;
        }
    }
}
