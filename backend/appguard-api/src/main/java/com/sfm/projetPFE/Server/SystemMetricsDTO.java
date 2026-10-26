package com.sfm.projetPFE.Server;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemMetricsDTO {
    private double cpuUser;
    private double cpuSystem;
    private double cpuIdle;
    private double memUsedPct;
    private long memTotal;
    private long memUsed;
    private double diskUsedPct;
    private long diskTotal;
    private long diskUsed;
    private LocalDateTime timestamp;
}

