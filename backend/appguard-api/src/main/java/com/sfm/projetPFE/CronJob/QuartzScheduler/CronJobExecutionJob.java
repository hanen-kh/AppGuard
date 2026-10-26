package com.sfm.projetPFE.CronJob.QuartzScheduler;


import com.sfm.projetPFE.CronJob.Cron.CronJob;
import com.sfm.projetPFE.CronJob.Cron.CronJobRepository;
import com.sfm.projetPFE.CronJob.CronExecution.CronExecutionServiceImpl;
import lombok.AllArgsConstructor;
import org.quartz.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class CronJobExecutionJob implements Job {

    @Autowired
    private CronExecutionServiceImpl cronExecutionService;

    @Autowired
    private CronJobRepository cronJobRepository;

    @Override
    public void execute(JobExecutionContext context) {
        Long cronJobId = context.getJobDetail().getJobDataMap().getLong("cronJobId");

        CronJob cronJob = cronJobRepository.findById(cronJobId)
                .orElseThrow(() -> new RuntimeException("CronJob not found"));

        cronExecutionService.executeCronJob(cronJob);
    }
}
