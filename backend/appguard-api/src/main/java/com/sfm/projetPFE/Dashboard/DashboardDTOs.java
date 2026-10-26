package com.sfm.projetPFE.Dashboard;

import com.sfm.projetPFE.Alert.AlertDto;
import com.sfm.projetPFE.CronJob.CronExecution.CronExecutionDtos;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor


public class DashboardDTOs {

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class DashboardData {
        private DashboardDTOs.ServerServiceStatsDto serverServiceStats;
        private DashboardDTOs.CronExecutionStatsDto cronExecutionStats;
        private DashboardDTOs.AlertStatsDto alertStats;
    }





    @AllArgsConstructor
    @Getter
    @Setter
    @NoArgsConstructor
    public static class ServerServiceStatsDto {
        private int totalServers;
        private int activeServers;
        private int maintenanceServers;
        private int unreachableServers;

        private int totalServices;
        private int activeServices;
        private int inactiveServices;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CronExecutionStatsDto {
        private int totalCronJobs;
        private long totalExecutions;
        private double successRate;
        private double averageExecutionTimeSeconds;
        private int failures;
        private int timeouts;
        private int agentUnreachable;

        private List<CronExecutionDtos.FailedExecutionDto> recentFailures;

    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class AlertStatsDto {
        private int totalAlerts;
        private int unresolvedAlerts;
        private int timeoutAlerts;
        private int failureAlerts;
        private int serverDownAlerts;

        private List<AlertDto> latestAlerts;
    }

}
