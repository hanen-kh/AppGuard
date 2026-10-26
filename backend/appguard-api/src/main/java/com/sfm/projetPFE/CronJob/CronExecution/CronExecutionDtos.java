package com.sfm.projetPFE.CronJob.CronExecution;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

public class CronExecutionDtos {

    @Getter
    @Setter

    public static class CommandRequest {
        private String command;
        private Integer timeoutMinutes;


    }

    @Getter
    @Setter
    public static class ExecutionResponse {
        private boolean success;
        private int exitCode;
        private String output;
        private String errorOutput;
        private Long memoryUsedKB;
        private Double cpuUsagePercent;

    }

    @Getter
    @Setter
    @AllArgsConstructor

    public static class CronExecutionUpdate {
        private final Long cronJobId;
        private final String cronJobName;
        private final ExecutionStatus status;
        private final String message;
        private final String serverName;
        private final Set<String> tags;
        private final Long memoryUsedKB;
        private final Double cpuUsagePercent;



    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FailedExecutionDto {
        private String cronJobName;
        private String serverName;
        private LocalDateTime startTime;
        private ExecutionStatus status;
        private String errorOutput;
        private int exitCode;
        private long durationMillis;
        private Long memoryUsedKB;
        private Double cpuUsagePercent;


    }

    @Getter
    @Setter

    public static class CronExecutionDto {
        private Long id;
        private LocalDateTime startTime;
        private String status;
        private Integer exitCode;
        private String output;
        private String errorOutput;
        private Long durationMillis;
        private LocalDateTime createdAt;
        private Long cronJobId;
        private String cronJobName; // <-- nouveau champ
        private Long memoryUsedKB;
        private Double cpuUsagePercent;

        // Constructeur vide
        public CronExecutionDto() {}

        // Constructeur avec tous les champs
        public CronExecutionDto(Long id, LocalDateTime startTime, String status,
                                Integer exitCode, String output, String errorOutput, Long durationMillis,
                                LocalDateTime createdAt, Long cronJobId, String cronJobName,
                                Long memoryUsedKB, Double cpuUsagePercent) {
            this.id = id;
            this.startTime = startTime;
            this.status = status;
            this.exitCode = exitCode;
            this.output = output;
            this.errorOutput = errorOutput;
            this.durationMillis = durationMillis;
            this.createdAt = createdAt;
            this.cronJobId = cronJobId;
            this.cronJobName = cronJobName; // <-- assignation
            this.memoryUsedKB = memoryUsedKB;
            this.cpuUsagePercent = cpuUsagePercent;
        }

    }


}
