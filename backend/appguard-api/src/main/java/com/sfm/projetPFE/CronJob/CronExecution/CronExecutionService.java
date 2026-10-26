package com.sfm.projetPFE.CronJob.CronExecution;

import com.sfm.projetPFE.CronJob.Cron.CronJob;

import java.util.List;

public interface CronExecutionService {

    CronExecution executeCronJob(CronJob cronJob);
    CronExecution createExecution(CronJob cronJob, ExecutionStatus status);
    List<CronExecution> getExecutionsByCronJob(Long cronJobId);
    List<CronExecution> getExecutionsByStatus(ExecutionStatus status);
    void deleteExecutionsByCronJob(Long cronJobId);
    void deleteOldExecutions(Long cronJobId, int maxToDelete);

    List<CronExecution> getExecutionsOfLastWeek();
}
