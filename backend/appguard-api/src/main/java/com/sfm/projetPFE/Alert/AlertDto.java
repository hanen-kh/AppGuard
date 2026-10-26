package com.sfm.projetPFE.Alert;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AlertDto {
    private String type;
    private String message;
    private LocalDateTime date;
    @Enumerated(EnumType.STRING)
    private AlertStatus status = AlertStatus.UNRESOLVED; // UNRESOLVED / RESOLVED
}
