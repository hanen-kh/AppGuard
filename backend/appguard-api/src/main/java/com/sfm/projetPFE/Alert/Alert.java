package com.sfm.projetPFE.Alert;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String type; // ex: "TIMEOUT", "FAILURE", "SERVER_DOWN"
    private String message;
    private LocalDateTime date = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private AlertStatus status = AlertStatus.UNRESOLVED; // UNRESOLVED / RESOLVED
}
