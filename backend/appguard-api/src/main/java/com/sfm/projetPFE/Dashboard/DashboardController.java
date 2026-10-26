package com.sfm.projetPFE.Dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired private DashboardServiceImpl dashboardService;


    @GetMapping("/overview")
    public ResponseEntity<DashboardDTOs.DashboardData> getDashboardOverview() {
        DashboardDTOs.ServerServiceStatsDto serverStats = dashboardService.getServerServiceStats();
        DashboardDTOs.CronExecutionStatsDto cronStats = dashboardService.getCronStats();
        DashboardDTOs.AlertStatsDto alertStats = dashboardService.getAlertStats();

        DashboardDTOs.DashboardData dashboardData = new DashboardDTOs.DashboardData(serverStats, cronStats, alertStats);
        return ResponseEntity.ok(dashboardData);
    }

}
