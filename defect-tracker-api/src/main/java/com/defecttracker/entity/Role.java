package com.defecttracker.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String roleName; // Super Admin, Project Manager, Tech Lead, Developer, QA Lead, QA Tester

    @Column(length = 100)
    private String type;

    @Column(length = 500)
    private String description;

    @JsonProperty("name")
    public String getName() {
        return roleName;
    }

    @JsonProperty("name")
    public void setName(String name) {
        if (this.roleName == null || this.roleName.isEmpty()) {
            this.roleName = name;
        }
    }
}
