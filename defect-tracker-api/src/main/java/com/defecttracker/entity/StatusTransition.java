package com.defecttracker.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "status_transitions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "from_status_id", nullable = false)
    private StatusType fromStatus;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "to_status_id", nullable = false)
    private StatusType toStatus;
}
