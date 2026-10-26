package com.sfm.projetPFE.LogEntry;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class LogEntry {
    private String id;

    private LocalDateTime timestamp;
    private String level;
    private String message;


    public LogEntry(String timestamp, String level, String message) {
        this.timestamp = LocalDateTime.parse(timestamp);
        this.level = level;
        this.message = message;
    }
}
