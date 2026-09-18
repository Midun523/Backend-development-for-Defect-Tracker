package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "status_types")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // New, Open, In Progress, Resolved, Closed, Reopened, Rejected

    @Column(length = 30)
    private String color;

    @Column(length = 50)
    private String type;

    @Column(length = 255)
    private String description;

    @Builder.Default
    private boolean isDefault = false;

    @Builder.Default
    private int orderIndex = 0;

    @Transient
    private Double positionX;

    @Transient
    private Double positionY;

    @com.fasterxml.jackson.annotation.JsonProperty("statusType")
    public String getStatusType() {
        return this.type;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("statusType")
    public void setStatusType(String statusType) {
        if (this.type == null || this.type.isEmpty()) {
            this.type = statusType;
        }
    }

    @com.fasterxml.jackson.annotation.JsonProperty("defectStatusName")
    public String getDefectStatusName() {
        return this.name;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("defectStatusName")
    public void setDefectStatusName(String defectStatusName) {
        if (this.name == null || this.name.isEmpty()) {
            this.name = defectStatusName;
        }
    }

    @com.fasterxml.jackson.annotation.JsonProperty("statusName")
    public String getStatusName() {
        return this.name;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("statusName")
    public void setStatusName(String statusName) {
        if (this.name == null || this.name.isEmpty()) {
            this.name = statusName;
        }
    }

    @com.fasterxml.jackson.annotation.JsonProperty("colorCode")
    public String getColorCode() {
        return this.color;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("colorCode")
    public void setColorCode(String colorCode) {
        if (this.color == null || this.color.isEmpty()) {
            this.color = colorCode;
        }
    }
}
