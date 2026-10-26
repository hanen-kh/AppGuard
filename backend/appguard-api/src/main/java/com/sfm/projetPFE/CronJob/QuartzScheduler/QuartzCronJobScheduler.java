package com.sfm.projetPFE.CronJob.QuartzScheduler;


import com.sfm.projetPFE.CronJob.Cron.CronJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Component;


import java.util.TimeZone;

@Component
public class QuartzCronJobScheduler {
    private static final Logger logger = LoggerFactory.getLogger(QuartzCronJobScheduler.class);

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;

    public void scheduleCronJob(CronJob cronJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        JobDetail jobDetail = JobBuilder.newJob(CronJobExecutionJob.class)
                .withIdentity("cronJob-" + cronJob.getId(), "cronJobs")
                .build();

        jobDetail.getJobDataMap().put("cronJobId", cronJob.getId());

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("trigger-" + cronJob.getId(), "cronJobs")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronJob.getCronExpression())
                        .inTimeZone(TimeZone.getTimeZone("Europe/Paris")))
                .build();

        if (scheduler.checkExists(jobDetail.getKey())) {
            scheduler.deleteJob(jobDetail.getKey());
        }

        scheduler.scheduleJob(jobDetail, trigger);
        logger.info("Scheduled CronJob ID: {}, Name: {}, Expression: {}",
                cronJob.getId(), cronJob.getName(), cronJob.getCronExpression());
    }

    public void unscheduleCronJob(Long cronJobId) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = new JobKey("cronJob-" + cronJobId, "cronJobs");
        if (scheduler.checkExists(jobKey)) {
            scheduler.deleteJob(jobKey);
            logger.info("Unscheduled CronJob ID: {}", cronJobId);
        }
    }

    public void rescheduleCronJob(CronJob cronJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();

        JobKey jobKey = new JobKey("cronJob-" + cronJob.getId(), "cronJobs");
        TriggerKey triggerKey = new TriggerKey("trigger-" + cronJob.getId(), "cronJobs");

        if (!scheduler.checkExists(jobKey)) {
            throw new SchedulerException("Job with ID " + cronJob.getId() + " is not scheduled");
        }

        Trigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .forJob(jobKey)
                .withSchedule(CronScheduleBuilder.cronSchedule(cronJob.getCronExpression())
                        .inTimeZone(TimeZone.getTimeZone("Europe/Paris")))
                .build();

        scheduler.rescheduleJob(triggerKey, newTrigger);
        logger.info("Rescheduled CronJob ID: {}, New Expression: {}",
                cronJob.getId(), cronJob.getCronExpression());
    }

}
