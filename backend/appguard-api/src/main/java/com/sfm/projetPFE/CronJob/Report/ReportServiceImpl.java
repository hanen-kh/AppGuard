package com.sfm.projetPFE.CronJob.Report;


import com.sfm.projetPFE.Alert.Alert;
import com.sfm.projetPFE.Alert.AlertRepository;
import com.sfm.projetPFE.Application.ApplicationRepository;
import com.sfm.projetPFE.Client.ClientRepository;
import com.sfm.projetPFE.CronJob.CronExecution.*;
import com.sfm.projetPFE.Server.Server;
import com.sfm.projetPFE.Server.ServerRepository;
import com.sfm.projetPFE.Server.ServerStatus;
import com.sfm.projetPFE.ServiceStatus.ServiceStatus;
import com.sfm.projetPFE.ServiceStatus.ServiceStatusRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired private ServerRepository serverRepository;
    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private ServiceStatusRepository serviceRepository;
    @Autowired private CronExecutionRepository cronExecutionRepository;
    @Autowired private AlertRepository alertRepository;
    @Autowired private CronExecutionServiceImpl cronExecutionService;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    // Exécution automatique chaque lundi à 8h
    //@Scheduled(cron = "0 0 8 * * MON")
    @Override
    @Transactional
    public ReportDataDto generateGlobalReport() {
        ReportDataDto report = new ReportDataDto();

        report.setCreatedAt(LocalDateTime.now());
        report.setCreatedBy("System (Manual trigger)");

        // 📌 Statistiques Clients / Serveurs
        report.setTotalClients((int) clientRepository.count());

        List<Server> servers = serverRepository.findAll();
        report.setTotalServers(servers.size());
        report.setActiveServers((int) servers.stream().filter(s -> s.getStatus() == ServerStatus.ACTIVE).count());
        report.setInactiveServers((int) servers.stream().filter(s -> s.getStatus() == ServerStatus.INACTIVE).count());

        report.setTotalApplications((int) applicationRepository.count());

        // 📌 Exécutions
        List<CronExecution> allExecutions = cronExecutionRepository.findAll();
        report.setTotalExecutions(allExecutions.size());

        long successCount = allExecutions.stream().filter(e -> e.getStatus() == ExecutionStatus.SUCCESS).count();
        report.setSuccessRate(allExecutions.isEmpty() ? 0.0 : (100.0 * successCount / allExecutions.size()));

        report.setFailures((int) allExecutions.stream().filter(e -> e.getStatus() == ExecutionStatus.FAILURE).count());
        report.setTimeouts((int) allExecutions.stream().filter(e -> e.getStatus() == ExecutionStatus.TIMEOUT).count());
        report.setAgentUnreachable((int) allExecutions.stream().filter(e -> e.getStatus() == ExecutionStatus.AGENT_UNREACHABLE).count());

        // Temps moyen
        double avgDuration = allExecutions.stream()
                .filter(e -> e.getDurationMillis() != null)
                .mapToLong(CronExecution::getDurationMillis)
                .average()
                .orElse(0.0);
        report.setAverageExecutionTimeSeconds(avgDuration / 1000);

        // 📌 Derniers échecs
        List<CronExecution> all = cronExecutionService.getExecutionsOfLastWeek();
        List<CronExecution> failures = all.stream()
                .filter(e -> List.of(ExecutionStatus.FAILURE, ExecutionStatus.TIMEOUT, ExecutionStatus.AGENT_UNREACHABLE)
                        .contains(e.getStatus()))
                .toList();

        List<CronExecutionDtos.FailedExecutionDto> recentFailures = failures.stream().map(exec -> {
            CronExecutionDtos.FailedExecutionDto dto = new CronExecutionDtos.FailedExecutionDto();
            dto.setCronJobName(exec.getCronJob().getName());
            dto.setServerName(exec.getCronJob().getServer().getName());
            dto.setStartTime(exec.getStartTime());
            dto.setStatus(exec.getStatus());
            dto.setErrorOutput(exec.getErrorOutput());
            dto.setExitCode(exec.getExitCode());
            dto.setDurationMillis(exec.getDurationMillis());
            dto.setMemoryUsedKB(exec.getMemoryUsedKB());
            dto.setCpuUsagePercent(exec.getCpuUsagePercent());
            return dto;
        }).toList();
        report.setRecentFailures(recentFailures);

        // 📌 Alertes
        List<Alert> alerts = alertRepository.findAll();
        report.setTotalAlerts(alerts.size());
        report.setTimeoutAlerts((int) alerts.stream().filter(a -> a.getType().equals("TIMEOUT")).count());
        report.setFailureAlerts((int) alerts.stream().filter(a -> a.getType().equals("FAILURE")).count());
        report.setServerDownAlerts((int) alerts.stream().filter(a -> a.getType().equals("SERVER_DOWN")).count());

        // Envoi via WebSocket (si besoin en temps réel)
        messagingTemplate.convertAndSend("/topic/reports/global", report);

        System.out.println("📊 Rapport généré à " + report.getCreatedAt());

        return report;
    }

}
