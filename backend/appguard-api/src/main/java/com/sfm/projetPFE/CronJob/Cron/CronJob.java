package com.sfm.projetPFE.CronJob.Cron;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.sfm.projetPFE.CronJob.CronExecution.CronExecution;
import com.sfm.projetPFE.CronJob.Tag.Tag;
import com.sfm.projetPFE.Server.Server;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;


@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CronJob implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;


    private String name;

    private String command;

    private String cronExpression; // planification de cron

    private String description;

    @Enumerated(EnumType.STRING)
    private CronStatus status = CronStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "server_id")
    private Server server;

    private Integer timeoutMinutes = 60;

    private Integer retryCount = 0;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime lastExecution;

    private LocalDateTime nextExecution;

    @OneToMany(mappedBy = "cronJob", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CronExecution> executions;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "cron_job_tags",
            joinColumns = @JoinColumn(name = "cron_job_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }


    public boolean canExecute() {
        return status == CronStatus.ACTIVE && nextExecution != null && !nextExecution.isAfter(LocalDateTime.now());
    }


    public String getExecutionBlockingReason() {
        if (status != CronStatus.ACTIVE) {
            return "Job is inactive";
        }
        if (nextExecution == null) {
            return "No valid next execution time";
        }
        if (nextExecution.isAfter(LocalDateTime.now())) {
            return "Next execution is scheduled in the future: " + nextExecution;
        }
        return "Job is executable";
    }

    public void updateNextExecution() {
        if (cronExpression == null || cronExpression.isBlank()) {
            this.nextExecution = null;
            throw new RuntimeException("Cron expression is null or blank");
        }
        try {
            CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
            CronParser parser = new CronParser(cronDefinition);
            Cron cron = parser.parse(cronExpression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            this.nextExecution = executionTime.nextExecution(ZonedDateTime.now())
                    .map(ZonedDateTime::toLocalDateTime)
                    .orElse(null);
            if (this.nextExecution == null) {
                throw new RuntimeException("Failed to calculate next execution for cron expression: " + cronExpression);
            }
        } catch (IllegalArgumentException e) {
            this.nextExecution = null;
            throw new RuntimeException("Invalid cron expression: " + cronExpression, e);
        }
    }

}
