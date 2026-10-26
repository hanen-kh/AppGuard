package com.sfm.projetPFE.CronJob.CronExecution;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.sfm.projetPFE.Alert.Alert;
import com.sfm.projetPFE.Alert.AlertServiceImpl;
import com.sfm.projetPFE.CronJob.Cron.CronJob;
import com.sfm.projetPFE.CronJob.Cron.CronJobRepository;
import com.sfm.projetPFE.CronJob.Tag.Tag;
import com.sfm.projetPFE.Role.TypeRole;
import com.sfm.projetPFE.Server.Server;
import com.sfm.projetPFE.User.User;
import com.sfm.projetPFE.User.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.*;

import java.awt.print.Pageable;
import java.time.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CronExecutionServiceImpl implements CronExecutionService {

    @Autowired
    private CronExecutionRepository cronExecutionRepository;

    @Autowired
    private CronJobRepository cronJobRepository;

    private static final Logger logger = LoggerFactory.getLogger(CronExecutionServiceImpl.class);

    @Autowired
    private  SimpMessagingTemplate messagingTemplate;
    @Autowired
    private RestTemplate restTemplate;

 @Autowired
 private UserRepository userRepository;

 @Autowired
 private AlertServiceImpl alertService;

    @Override
    @Transactional
    public CronExecution executeCronJob(CronJob cronJob) {
        logger.info("Starting execution for CronJob ID: {}", cronJob.getId());

        // Précharger les superviseurs pour éviter LazyInitializationException
       // cronJob.getServer().getClient().getSupervisors().size();

        CronExecution execution = new CronExecution();
        execution.setCronJob(cronJob);
        execution.setStartTime(LocalDateTime.now());
        execution.setStatus(ExecutionStatus.RUNNING);
        cronExecutionRepository.save(execution);

        try {
            //  Exécution via Agent
            CronExecutionDtos.ExecutionResponse result = executeViaAgent(cronJob);

            execution.setEndTime(LocalDateTime.now());
            execution.setStatus(result.isSuccess() ? ExecutionStatus.SUCCESS : ExecutionStatus.FAILURE);
            execution.setExitCode(result.getExitCode());
            execution.setOutput(result.getOutput());
            execution.setErrorOutput(result.getErrorOutput());
            execution.setDurationMillis(Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis());
            execution.setMemoryUsedKB(result.getMemoryUsedKB());
            execution.setCpuUsagePercent(result.getCpuUsagePercent());

            // ⏱️ Alerte si TIMEOUT
            if (cronJob.getTimeoutMinutes() != null &&
                    execution.getDurationMillis() > cronJob.getTimeoutMinutes() * 60_000L) {
                logger.warn("Execution exceeded configured timeout for CronJob ID: {}", cronJob.getId());
                execution.setStatus(ExecutionStatus.TIMEOUT);

                Alert alert = alertService.triggerAlert(
                        "TIMEOUT",
                        String.format("Execution of CronJob '%s' exceeded timeout of %d min",
                                cronJob.getName(), cronJob.getTimeoutMinutes())
                );
                //alertService.notifyAdminsAndSupervisors(cronJob.getServer(), alert);
            }

            //  Alerte si échec
            if (!result.isSuccess()) {
                Alert alert = alertService.triggerAlert(
                        "FAILURE",
                        String.format("Execution failed for CronJob '%s' with exit code %d",
                                cronJob.getName(), result.getExitCode())
                );
               // alertService.notifyAdminsAndSupervisors(cronJob.getServer(), alert);
            }

            cronExecutionRepository.save(execution);

            // 🔁 Mise à jour du cron job
            cronJob.setLastExecution(execution.getStartTime());
            cronJob.setNextExecution(calculateNextExecution(cronJob.getCronExpression()));
            cronJobRepository.save(cronJob);



            logger.info("Execution completed for CronJob ID: {}, Status: {}", cronJob.getId(), execution.getStatus());
            return execution;

        } catch (RuntimeException e) {
            logger.error("Failed to execute CronJob ID: {} via API Agent: {}", cronJob.getId(), e.getMessage());
            execution.setEndTime(LocalDateTime.now());

            if (e.getCause() instanceof ResourceAccessException &&
                    e.getMessage().contains("Read timed out")) {
                execution.setStatus(ExecutionStatus.TIMEOUT);
            } else {
                execution.setStatus(ExecutionStatus.FAILURE);
            }

            execution.setExitCode(-1);
            execution.setErrorOutput(e.getMessage());
            execution.setDurationMillis(Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis());
            cronExecutionRepository.save(execution);

            // ⚠️ Alerte en cas d'erreur agent
            Alert alert = alertService.triggerAlert(
                    "AGENT_UNREACHABLE",
                    String.format("Error executing CronJob '%s': %s", cronJob.getName(), e.getMessage())
            );
          //  alertService.notifyAdminsAndSupervisors(cronJob.getServer(), alert);

            throw e;
        }
    }



    @Override
    @Transactional
    public CronExecution createExecution(CronJob cronJob, ExecutionStatus status) {
        logger.info("Creating execution for CronJob ID: {} with status: {}", cronJob.getId(), status);

        // Validate inputs
        if (cronJob == null) {
            throw new IllegalArgumentException("CronJob cannot be null");
        }
        if (status == null) {
            throw new IllegalArgumentException("Execution status cannot be null");
        }
        if (!cronJob.canExecute() && status == ExecutionStatus.RUNNING) {
            throw new RuntimeException("Cannot create RUNNING execution: " + cronJob.getExecutionBlockingReason());
        }

        // Create execution
        CronExecution execution = new CronExecution();
        execution.setCronJob(cronJob);
        execution.setStartTime(LocalDateTime.now());
        execution.setStatus(status);
        execution.setCreatedAt(LocalDateTime.now());

        // Update cron job
        cronJob.getExecutions().add(execution);
        cronJob.setLastExecution(LocalDateTime.now());
        cronJob.updateNextExecution();

        // Persist execution
        cronExecutionRepository.save(execution);



        logger.info("Execution created for CronJob ID: {}, Status: {}", cronJob.getId(), status);
        return execution;
    }
    @Override
    @Transactional(readOnly = true)
    public List<CronExecution> getExecutionsByCronJob(Long cronJobId) {
        logger.debug("Retrieving executions for CronJob ID: {}", cronJobId);
        return cronExecutionRepository.findByCronJobIdOrderByStartTimeDesc(cronJobId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CronExecution> getExecutionsByStatus(ExecutionStatus status) {
        logger.debug("Retrieving executions with status: {}", status);
        return cronExecutionRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public void deleteExecutionsByCronJob(Long cronJobId) {
        logger.info("Deleting executions for CronJob ID: {}", cronJobId);
        cronExecutionRepository.deleteByCronJobId(cronJobId);
    }


    @Retryable(value = RestClientException.class, maxAttemptsExpression = "#{@cronJob.retryCount}", backoff = @Backoff(delay = 5000))
    @CircuitBreaker(name = "agent", fallbackMethod = "fallbackExecuteViaAgent")
    private CronExecutionDtos.ExecutionResponse executeViaAgent(CronJob cronJob) {
        Server server = cronJob.getServer();
        String url = server.getAgentUrl() + "/execute";

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(cronJob.getTimeoutMinutes() * 60 * 1000);
        restTemplate.setRequestFactory(factory);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + server.getAgentApiKey());

        CronExecutionDtos.CommandRequest requestBody = new CronExecutionDtos.CommandRequest();
        requestBody.setCommand(cronJob.getCommand());
        requestBody.setTimeoutMinutes(cronJob.getTimeoutMinutes());

        HttpEntity<CronExecutionDtos.CommandRequest> request = new HttpEntity<>(requestBody, headers);

        int attempts = cronJob.getRetryCount() + 1;

        for (int i = 0; i < attempts; i++) {
            try {
                logger.debug("Attempt {}/{} to execute CronJob ID: {} on agent: {}", i + 1, attempts, cronJob.getId(), url);
                ResponseEntity<CronExecutionDtos.ExecutionResponse> response = restTemplate
                        .postForEntity(url, request, CronExecutionDtos.ExecutionResponse.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    logger.info("CronJob ID: {} executed successfully on attempt {}/{}", cronJob.getId(), i + 1, attempts);
                    return response.getBody();
                } else {
                    logger.warn("Invalid response from API Agent: {}", response.getStatusCodeValue());
                }

            } catch (HttpClientErrorException | HttpServerErrorException e) {
                logger.warn("Retry {}/{} failed for CronJob ID: {}, error: {}", i + 1, attempts, cronJob.getId(), e.getMessage());

                if (i == attempts - 1) {
                    // Sauvegarde d’une exécution échouée
                    CronExecution execution = new CronExecution();
                    execution.setCronJob(cronJob);
                    execution.setStartTime(LocalDateTime.now());
                    execution.setEndTime(LocalDateTime.now());
                    execution.setStatus(ExecutionStatus.AGENT_UNREACHABLE);
                    execution.setExitCode(-1);
                    execution.setErrorOutput("Agent unreachable after " + attempts + " attempts: " + e.getMessage());
                    execution.setDurationMillis(0L);
                    cronExecutionRepository.save(execution);

                    // Alerte
                    Alert alert = alertService.triggerAlert(
                            "AGENT_UNREACHABLE",
                            String.format("Agent failed for CronJob '%s' after %d attempts: %s",
                                    cronJob.getName(), attempts, e.getMessage())
                    );

                    // Retourne une réponse d’échec
                    CronExecutionDtos.ExecutionResponse failureResponse = new CronExecutionDtos.ExecutionResponse();
                    failureResponse.setSuccess(false);
                    failureResponse.setExitCode(-1);
                    failureResponse.setErrorOutput("Agent unreachable after retries: " + e.getMessage());
                    return failureResponse;
                }

            }
        }

        // Par défaut : réponse d’échec générique
        CronExecutionDtos.ExecutionResponse unknownFailure = new CronExecutionDtos.ExecutionResponse();
        unknownFailure.setSuccess(false);
        unknownFailure.setExitCode(-1);
        unknownFailure.setErrorOutput("Unknown failure - all retries exhausted.");
        return unknownFailure;
    }


    private CronExecutionDtos.ExecutionResponse fallbackExecuteViaAgent(CronJob cronJob, Throwable t) {
        logger.error("Fallback triggered for CronJob ID: {}, error: {}", cronJob.getId(), t.getMessage());

        // ✅ Génération de l'alerte
        String message = String.format("Agent unreachable for CronJob '%s' (Server: %s, Client: %s). Error: %s",
                cronJob.getName(),
                cronJob.getServer().getName(),
                cronJob.getServer().getClient().getName(),
                t.getMessage());

        Alert alert = alertService.triggerAlert("AGENT_UNREACHABLE", message);

        // ✅ Envoi aux superviseurs du client + admins
        Set<User> supervisors = cronJob.getServer().getClient().getSupervisors();
        List<User> admins = userRepository.findByRole_Type(TypeRole.ADMIN);

        Set<User> recipients = new HashSet<>(supervisors);
        recipients.addAll(admins);

        for (User user : recipients) {
            messagingTemplate.convertAndSend("/topic/admin/alerts/" + user.getId(), alert);
        }


        CronExecutionDtos.ExecutionResponse response = new CronExecutionDtos.ExecutionResponse();
        response.setSuccess(false);
        response.setExitCode(-1);
        response.setErrorOutput("Agent unavailable: " + t.getMessage());
        return response;
    }




    private LocalDateTime calculateNextExecution(String cronExpression) {
        try {
            CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
            CronParser parser = new CronParser(cronDefinition);
            Cron cron = parser.parse(cronExpression);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            return executionTime.nextExecution(ZonedDateTime.now()).map(ZonedDateTime::toLocalDateTime).orElse(null);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid cron expression: {}, error: {}", cronExpression, e.getMessage());
            return null;
        }
    }


    @Override
    public void deleteOldExecutions(Long cronJobId, int maxToDelete) {
        Pageable pageable = (Pageable) PageRequest.of(0, maxToDelete, Sort.by("startTime").ascending());

        List<CronExecution> oldestExecutions =
                cronExecutionRepository.findByCronJob_Id(cronJobId, (org.springframework.data.domain.Pageable) pageable);

        cronExecutionRepository.deleteAll(oldestExecutions);
    }


    @Override
    public List<CronExecution> getExecutionsOfLastWeek() {
        // Calculer la semaine précédente (lundi au dimanche)
        LocalDate today = LocalDate.now();
        LocalDate lastMonday = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate lastSunday = lastMonday.plusDays(6);

        LocalDateTime startOfWeek = lastMonday.atStartOfDay();              // Lundi 00:00
        LocalDateTime endOfWeek = lastSunday.atTime(LocalTime.MAX);         // Dimanche 23:59:59.999

        return cronExecutionRepository.findByStartTimeBetween(startOfWeek, endOfWeek);
    }


    public class CronExecutionMapper {
        public static CronExecutionDtos.CronExecutionDto toDto(CronExecution execution) {
            return new CronExecutionDtos.CronExecutionDto(
                    execution.getId(),
                    execution.getStartTime(),
                    execution.getStatus() != null ? execution.getStatus().name() : null,
                    execution.getExitCode(),
                    execution.getOutput(),
                    execution.getErrorOutput(),
                    execution.getDurationMillis(),
                    execution.getCreatedAt(),
                    execution.getCronJob() != null ? execution.getCronJob().getId() : null,
                    execution.getCronJob() != null ? execution.getCronJob().getName() : null, // <-- nom du Cron Job
                    execution.getMemoryUsedKB(),
                    execution.getCpuUsagePercent()
            );
        }
    }



    public List<CronExecutionDtos.CronExecutionDto> getAllExecutions() {
        return cronExecutionRepository.findAll()
                .stream()
                .map(CronExecutionMapper::toDto)
                .collect(Collectors.toList());
    }

}
