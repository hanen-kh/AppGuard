package com.sfm.projetPFE.CronJob.Cron;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;
import java.util.Set;

public class CronJobDtos {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateCronRequest {
        @NotBlank(message = "Name is mandatory")
        private String name;

        @NotBlank(message = "Command is mandatory")
        private String command;

        @NotBlank(message = "Cron expression is mandatory")
        private String cronExpression;

        private String description;

        @NotNull(message = "Server ID is mandatory")
        private Long serverId;

        private Set<Long> tagIds;

        private Integer timeoutMinutes;

        private Integer retryCount;


    }

    @Getter
    @Setter
    @AllArgsConstructor
    @Data
    public static class CronJobResponse {
        private  Long id;
        private String name;
        private String command;
        private  String cronExpression;
        private  String description;
        private String serverName;     // seulement le nom du serveur
        private List<String> tags;
        private  Integer timeoutMinutes;
        private  Integer retryCount;
        private  CronStatus status;

        public CronJobResponse() {

        }


        @Getter
        @Setter
        @AllArgsConstructor
        public static class ServerDto {
            private  Long id;
            private  String hostname;


        }
         @Getter
         @Setter
         @AllArgsConstructor
        public static class TagDto {
            private  Long id;
            private  String name;


        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class CronJobUpdate {
        private final Long cronJobId;
        private final String cronJobName;
        private final String status;
        private final String message;
        private final String serverName;
        private final Set<String> tags;


    }
}
