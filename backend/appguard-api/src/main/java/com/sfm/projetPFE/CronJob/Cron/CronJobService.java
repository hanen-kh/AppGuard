package com.sfm.projetPFE.CronJob.Cron;

import java.util.List;
import java.util.Optional;

public interface CronJobService {
    void createCronJob(Long serverId, CronJobDtos.CreateCronRequest request);
    CronJob updateCronJob(Long id, CronJob updatedCronJob);
    void deleteCronJob(Long id);
    Optional<CronJob> getCronJob(Long id);
    List<CronJobDtos.CronJobResponse> getAllCronJobs();
    void scheduleCronJob(Long id);
    boolean validateCronExpression(String cronExpression);
    void activateCronJobs(List<Long> ids);
    void deactivateCronJobs(List<Long> ids);

}
