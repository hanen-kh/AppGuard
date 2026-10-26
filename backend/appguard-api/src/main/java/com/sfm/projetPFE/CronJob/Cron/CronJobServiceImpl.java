package com.sfm.projetPFE.CronJob.Cron;


import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.sfm.projetPFE.AppGuardLogs.LogService;
import com.sfm.projetPFE.CronJob.QuartzScheduler.QuartzCronJobScheduler;
import com.sfm.projetPFE.CronJob.Tag.Tag;
import com.sfm.projetPFE.Server.Server;
import com.sfm.projetPFE.Server.ServerRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@NoArgsConstructor
public class CronJobServiceImpl implements CronJobService {

    @Autowired
    private QuartzCronJobScheduler quartzScheduler;

    @Autowired
    private CronJobRepository cronJobRepository;
    private static final Logger logger = LoggerFactory.getLogger(LogService.class);
    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private SchedulerFactoryBean schedulerFactoryBean;


    @Override
    @Transactional
    public void createCronJob(Long serverId, CronJobDtos.CreateCronRequest request) {
        logger.info("Creating CronJob '{}' for Server ID {}", request.getName(), serverId);

        // Validation du serveur
        Server server = serverRepository.findById(serverId)
                .orElseThrow(() -> new RuntimeException("Server not found with ID: " + serverId));

        if (server.getAgentUrl() == null || server.getAgentUrl().isBlank()) {
            throw new IllegalArgumentException("Server must have a valid agent URL");
        }
        if (server.getAgentApiKey() == null || server.getAgentApiKey().isBlank()) {
            throw new IllegalArgumentException("Server must have a valid agent API key");
        }

        // Validation du cron job
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("CronJob name is required");
        }
        if (request.getCommand() == null || request.getCommand().isBlank()) {
            throw new IllegalArgumentException("Command is required");
        }
        if (request.getCronExpression() == null || request.getCronExpression().isBlank()) {
            throw new IllegalArgumentException("Cron expression is required");
        }

        // Validation expression cron
        if (!validateCronExpression(request.getCronExpression())) {
            throw new IllegalArgumentException("Invalid cron expression: " + request.getCronExpression());
        }

        // Création de l'objet CronJob
        CronJob job = new CronJob();
        job.setName(request.getName());
        job.setCommand(request.getCommand());
        job.setCronExpression(request.getCronExpression());
        job.setDescription(request.getDescription());
        job.setTimeoutMinutes(request.getTimeoutMinutes() != null && request.getTimeoutMinutes() > 0
                ? request.getTimeoutMinutes() : 1);
        job.setRetryCount(request.getRetryCount() != null && request.getRetryCount() >= 0
                ? request.getRetryCount() : 1);
        job.setStatus(CronStatus.ACTIVE);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        job.setServer(server);

        job.updateNextExecution();

        // Sauvegarde
        CronJob savedJob = cronJobRepository.save(job);

        // ➕ Ajout à Quartz
        try {
            quartzScheduler.scheduleCronJob(savedJob);
        } catch (Exception e) {
            logger.error("Failed to schedule job in Quartz", e);
            throw new RuntimeException("Job creation succeeded, but scheduling failed");
        }

        // ✅ Pas de return - méthode void
    }

    @Override
    @Transactional
    public CronJob updateCronJob(Long id, CronJob updatedCronJob) {
        logger.info("Updating CronJob ID: {}", id);

        // Validate inputs
        if (id == null) {
            throw new IllegalArgumentException("CronJob ID cannot be null");
        }
        if (updatedCronJob == null) {
            throw new IllegalArgumentException("Updated CronJob cannot be null");
        }

        // Find existing cron job
        Optional<CronJob> existingCronJobOpt = cronJobRepository.findById(id);
        if (existingCronJobOpt.isEmpty()) {
            throw new RuntimeException("CronJob with ID " + id + " not found");
        }

        CronJob existingCronJob = existingCronJobOpt.get();

        // Update fields
        if (updatedCronJob.getName() != null && !updatedCronJob.getName().isBlank()) {
            existingCronJob.setName(updatedCronJob.getName());
        }
        if (updatedCronJob.getCommand() != null && !updatedCronJob.getCommand().isBlank()) {
            existingCronJob.setCommand(updatedCronJob.getCommand());
        }
        if (updatedCronJob.getCronExpression() != null && !updatedCronJob.getCronExpression().isBlank()) {
            if (!validateCronExpression(updatedCronJob.getCronExpression())) {
                throw new RuntimeException("Invalid cron expression: " + updatedCronJob.getCronExpression());
            }
            existingCronJob.setCronExpression(updatedCronJob.getCronExpression());
        }
        if (updatedCronJob.getRetryCount() > 0) {
            existingCronJob.setRetryCount(updatedCronJob.getRetryCount());
        }
        if (updatedCronJob.getTimeoutMinutes() > 0) {
            existingCronJob.setTimeoutMinutes(updatedCronJob.getTimeoutMinutes());
        }
        if (updatedCronJob.getServer() != null) {
            Server server = getServer(updatedCronJob);
            existingCronJob.setServer(server);
        }
        if (updatedCronJob.getTags() != null) {
            existingCronJob.setTags(updatedCronJob.getTags());
        }
        if (updatedCronJob.getStatus() != null) {
            existingCronJob.setStatus(updatedCronJob.getStatus());
        }

        // Update next execution
        existingCronJob.updateNextExecution();

        // Persist the updated cron job
        CronJob savedCronJob = cronJobRepository.save(existingCronJob);
        // ➕ Ajout à Quartz
        try {
            quartzScheduler.rescheduleCronJob(savedCronJob);
        } catch (Exception e) {
            logger.error("Failed to reschedule job in Quartz", e);
            throw new RuntimeException("Job update succeeded, but rescheduling failed");
        }
        logger.info("CronJob ID: {} updated", id);
        return savedCronJob;
    }

    private static Server getServer(CronJob updatedCronJob) {
        Server server = updatedCronJob.getServer();
        if (server.getAgentUrl() == null || server.getAgentUrl().isBlank()) {
            throw new IllegalArgumentException("Server must have a valid agent URL");
        }
        if (server.getAgentApiKey() == null || server.getAgentApiKey().isBlank()) {
            throw new IllegalArgumentException("Server must have a valid agent API key");
        }

        return server;
    }

    @Override
    @Transactional
    public void deleteCronJob(Long id) {
        logger.info("Deleting CronJob ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("CronJob ID cannot be null");
        }

        if (!cronJobRepository.existsById(id)) {
            throw new RuntimeException("CronJob with ID " + id + " not found");
        }

        cronJobRepository.deleteById(id);

        // ➕ Ajout à Quartz
        try {
            quartzScheduler.unscheduleCronJob(id);
        } catch (Exception e) {
            logger.error("Failed to unschedule job in Quartz", e);
            throw new RuntimeException("Job delete succeeded, but unscheduling failed");
        }

        logger.info("CronJob ID: {} deleted", id);
    }

    @Override
    public Optional<CronJob> getCronJob(Long id) {
        logger.info("Retrieving CronJob ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("CronJob ID cannot be null");
        }

        return cronJobRepository.findById(id);
    }



    @Override
    @Transactional
    public void scheduleCronJob(Long id) {
        logger.info("Scheduling CronJob ID: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("CronJob ID cannot be null");
        }

        Optional<CronJob> cronJobOpt = cronJobRepository.findById(id);
        if (cronJobOpt.isEmpty()) {
            throw new RuntimeException("CronJob with ID " + id + " not found");
        }

        CronJob cronJob = cronJobOpt.get();
        if (!validateCronExpression(cronJob.getCronExpression())) {
            throw new RuntimeException("Invalid cron expression: " + cronJob.getCronExpression());
        }

        cronJob.updateNextExecution();
        cronJob.setStatus(CronStatus.ACTIVE);
        cronJobRepository.save(cronJob);

        logger.info("CronJob ID: {} scheduled with next execution: {}", id, cronJob.getNextExecution());
    }

    @Override
    public boolean validateCronExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.isBlank()) {
            logger.warn("Cron expression is null or blank");
            return false;
        }
        try {
            CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
            CronParser parser = new CronParser(cronDefinition);

            // Pour une exécution toutes les 2 minutes, utilisez ce format :
            // "0 */2 * * * ?" (le "?" remplace * pour le jour de la semaine)
            parser.parse(cronExpression);
            return true;
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid Quartz cron expression '{}': {}", cronExpression, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public void activateCronJobs(List<Long> ids) {
        List<CronJob> jobs = cronJobRepository.findAllById(ids);

        for (CronJob job : jobs) {
            if (!validateCronExpression(job.getCronExpression())) continue;

            job.setStatus(CronStatus.ACTIVE);
            job.updateNextExecution();

            try {
                JobKey jobKey = new JobKey("cronJob-" + job.getId(), "cronJobs");
                if (schedulerFactoryBean.getScheduler().checkExists(jobKey)) {
                    schedulerFactoryBean.getScheduler().resumeJob(jobKey); // ✅ Résume la tâche si elle existe
                } else {
                    quartzScheduler.scheduleCronJob(job); // ✅ Sinon, la programme
                }
            } catch (SchedulerException e) {
                logger.error("Failed to activate job ID {}: {}", job.getId(), e.getMessage());
            }
        }

        cronJobRepository.saveAll(jobs);
    }



    @Override
    @Transactional
    public void deactivateCronJobs(List<Long> ids) {
        List<CronJob> jobs = cronJobRepository.findAllById(ids);

        for (CronJob job : jobs) {
            job.setStatus(CronStatus.INACTIVE);
            job.setNextExecution(null); // désactive logiquement

            try {
                JobKey jobKey = new JobKey("cronJob-" + job.getId(), "cronJobs");
                if (schedulerFactoryBean.getScheduler().checkExists(jobKey)) {
                    schedulerFactoryBean.getScheduler().pauseJob(jobKey); // ✅ Pause sans supprimer
                }
            } catch (SchedulerException e) {
                logger.error("Failed to deactivate job ID {}: {}", job.getId(), e.getMessage());
            }
        }

        cronJobRepository.saveAll(jobs);
    }


    @PostConstruct
    public void init() {
        List<CronJob> activeJobs = cronJobRepository.findByStatus(CronStatus.ACTIVE);
        for (CronJob job : activeJobs) {
            System.out.println(1);
            try {
                quartzScheduler.scheduleCronJob(job);
            } catch (Exception e) {

            }
        }
    }


    @Override
    public List<CronJobDtos.CronJobResponse> getAllCronJobs() {
        List<CronJob> cronJobs = cronJobRepository.findAll();

        return cronJobs.stream()
                .map(cron -> {
                    CronJobDtos.CronJobResponse response = new CronJobDtos.CronJobResponse();
                    response.setId(cron.getId());
                    response.setName(cron.getName());
                    response.setCommand(cron.getCommand());
                    response.setCronExpression(cron.getCronExpression());
                    response.setDescription(cron.getDescription());
                    response.setTimeoutMinutes(cron.getTimeoutMinutes());
                    response.setRetryCount(cron.getRetryCount());
                    response.setStatus(cron.getStatus());

                    if (cron.getServer() != null) {
                        response.setServerName(cron.getServer().getName());
                    }

                    if (cron.getTags() != null) {
                        List<String> tagNames = cron.getTags().stream()
                                .map(Tag::getName)
                                .toList();
                        response.setTags(tagNames);
                    } else {
                        response.setTags(List.of());
                    }

                    return response;
                })
                .toList();
    }


}
