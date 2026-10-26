package com.sfm.projetPFE.CronJob.Report;

import com.sfm.projetPFE.CronJob.CronExecution.CronExecutionDtos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReportDataDto {
    private String type = "GLOBAL_STATS";
    private LocalDateTime createdAt;
    private String createdBy;

    private int totalClients;
    private int totalServers;
    private int activeServers;
    private int inactiveServers;

    private int totalApplications;
    private int totalServices;
    private int greenServices;
    private int yellowServices;
    private int redServices;

    private long totalExecutions;
    private double successRate;
    private int failures;
    private int timeouts;
    private int agentUnreachable;
    private double averageExecutionTimeSeconds;
    private List<CronExecutionDtos.FailedExecutionDto> recentFailures;

    private int totalAlerts;
    private int timeoutAlerts;
    private int failureAlerts;
    private int serverDownAlerts;

}
