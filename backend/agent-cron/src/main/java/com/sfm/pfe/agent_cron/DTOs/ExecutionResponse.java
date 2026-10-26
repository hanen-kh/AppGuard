package com.sfm.pfe.agent_cron.DTOs;

import lombok.Data;

@Data
public class ExecutionResponse {
    private boolean success;
    private int exitCode;
    private String output;
    private String errorOutput;
    private long durationMillis;
    private long memoryUsedKB;
    private double cpuUsagePercent;
}
