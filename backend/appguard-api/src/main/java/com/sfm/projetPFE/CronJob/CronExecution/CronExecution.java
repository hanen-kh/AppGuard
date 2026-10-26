package com.sfm.projetPFE.CronJob.CronExecution;


import com.sfm.projetPFE.CronJob.Cron.CronJob;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CronExecution implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;



    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;

    private Integer exitCode;

    private String output;

    private String errorOutput;

    private Long durationMillis;

    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cron_job_id")
    private CronJob cronJob;

    private Long memoryUsedKB;
    private Double cpuUsagePercent;

}
