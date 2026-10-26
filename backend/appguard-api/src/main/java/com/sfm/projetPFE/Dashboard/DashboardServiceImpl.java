package com.sfm.projetPFE.Dashboard;


import com.sfm.projetPFE.Alert.Alert;
import com.sfm.projetPFE.Alert.AlertDto;
import com.sfm.projetPFE.Alert.AlertRepository;
import com.sfm.projetPFE.Alert.AlertStatus;
import com.sfm.projetPFE.CronJob.Cron.CronJobRepository;
import com.sfm.projetPFE.CronJob.CronExecution.CronExecution;
import com.sfm.projetPFE.CronJob.CronExecution.CronExecutionDtos;
import com.sfm.projetPFE.CronJob.CronExecution.CronExecutionRepository;
import com.sfm.projetPFE.CronJob.CronExecution.ExecutionStatus;
import com.sfm.projetPFE.Server.Server;
import com.sfm.projetPFE.Server.ServerRepository;
import com.sfm.projetPFE.Server.ServerStatus;
import com.sfm.projetPFE.ServiceStatus.ServiceStatus;
import com.sfm.projetPFE.ServiceStatus.ServiceStatusRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class DashboardServiceImpl {
    @Autowired
    private ServerRepository serverRepository;

    @Autowired
    private ServiceStatusRepository serviceRepository;

    @Autowired
    private CronJobRepository cronJobRepository;

    @Autowired
    private CronExecutionRepository cronExecutionRepository;

    @Autowired
    private AlertRepository alertRepository;


    // SECTION 1 - Serveurs & Services
    public DashboardDTOs.ServerServiceStatsDto getServerServiceStats() {
        List<Server> servers = serverRepository.findAll();
        List<ServiceStatus> services = serviceRepository.findAll();

        int totalServers = servers.size();
        int activeServers = (int) servers.stream().filter(s -> s.getStatus() == ServerStatus.ACTIVE).count();
        int maintenanceServers = (int) servers.stream().filter(s -> s.getStatus() == ServerStatus.MAINTENANCE).count();
        int unreachableServers = (int) servers.stream().filter(s -> s.getStatus() == ServerStatus.UNREACHABLE).count();

        int totalServices = services.size();
        int activeServices = (int) services.stream().filter(s -> Boolean.TRUE.equals(s.getIsActive()))
                .count();
        int inactiveServices = totalServices - activeServices;

        return new DashboardDTOs.ServerServiceStatsDto(
                totalServers,
                activeServers,
                maintenanceServers,
                unreachableServers,
                totalServices,
                activeServices,
                inactiveServices
        );
    }

    // SECTION 2 - Crons
    public DashboardDTOs.CronExecutionStatsDto getCronStats() {
        List<CronExecution> executions = cronExecutionRepository.findAll();

        DashboardDTOs.CronExecutionStatsDto dto = new DashboardDTOs.CronExecutionStatsDto();
        dto.setTotalCronJobs((int) cronJobRepository.count());
        dto.setTotalExecutions(executions.size());

        long successCount = executions.stream().filter(e -> e.getStatus() == ExecutionStatus.SUCCESS).count();
        dto.setSuccessRate(executions.isEmpty() ? 0.0 : 100.0 * successCount / executions.size());

        dto.setFailures((int) executions.stream().filter(e -> e.getStatus() == ExecutionStatus.FAILURE).count());
        dto.setTimeouts((int) executions.stream().filter(e -> e.getStatus() == ExecutionStatus.TIMEOUT).count());
        dto.setAgentUnreachable((int) executions.stream().filter(e -> e.getStatus() == ExecutionStatus.AGENT_UNREACHABLE).count());

        double avg = executions.stream()
                .filter(e -> e.getDurationMillis() != null)
                .mapToLong(CronExecution::getDurationMillis)
                .average()
                .orElse(0.0);
        dto.setAverageExecutionTimeSeconds(avg / 1000);

        // Dernières erreurs
        List<CronExecution> failures = cronExecutionRepository.findTop10ByStatusIn(
                List.of(ExecutionStatus.FAILURE, ExecutionStatus.TIMEOUT, ExecutionStatus.AGENT_UNREACHABLE),
                Sort.by(Sort.Direction.DESC, "startTime")
        );


        List<CronExecutionDtos.FailedExecutionDto> failedDtos = failures.stream().map(e -> {
            CronExecutionDtos.FailedExecutionDto f = new CronExecutionDtos.FailedExecutionDto();
            f.setCronJobName(e.getCronJob().getName());
            f.setServerName(e.getCronJob().getServer().getName());
            f.setStartTime(e.getStartTime());
            f.setStatus(e.getStatus());
            f.setErrorOutput(e.getErrorOutput());
            f.setExitCode(e.getExitCode());
            f.setDurationMillis(e.getDurationMillis());
            f.setMemoryUsedKB(e.getMemoryUsedKB());
            f.setCpuUsagePercent(e.getCpuUsagePercent());
            return f;
        }).collect(Collectors.toList());

        dto.setRecentFailures(failedDtos);
        return dto;
    }

    // SECTION 3 - Alertes
    public DashboardDTOs.AlertStatsDto getAlertStats() {
        List<Alert> alerts = alertRepository.findAll();

        int unresolved = (int) alerts.stream().filter(a -> a.getStatus() == AlertStatus.UNRESOLVED).count();

        List<AlertDto> latest = alerts.stream()
                .sorted(Comparator.comparing(Alert::getDate).reversed())
                .limit(10)
                .map(a -> {
                    AlertDto dto = new AlertDto();
                    dto.setType(a.getType());
                    dto.setMessage(a.getMessage());
                    dto.setDate(a.getDate());
                    dto.setStatus(a.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());

        return new DashboardDTOs.AlertStatsDto(
                alerts.size(),
                unresolved,
                (int) alerts.stream().filter(a -> "TIMEOUT".equals(a.getType())).count(),
                (int) alerts.stream().filter(a -> "FAILURE".equals(a.getType())).count(),
                (int) alerts.stream().filter(a -> "AGENT_UNREACHABLE".equals(a.getType())).count(),
                latest
        );
    }

}