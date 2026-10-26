package com.sfm.projetPFE.AppGuardLogs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "appguardlogs")
public class Log implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    private String id;
    private int idUser;
    private String serviceName;
    private String methodName;
    @Enumerated(EnumType.STRING)
    private LogLevel level;
    private LocalDateTime timestamp;
    private long latency;//en milliseconde
    private String ipAddress;

    private String stackTrace; // Pour le stockage des détails d'exception si nécessaire
}
