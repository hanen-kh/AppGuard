package com.sfm.projetPFE.Server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    @Autowired
    private ServerServiceImpl serverService;

    /**
     * 🔧 Met un serveur en mode maintenance et désactive ses cron jobs
     */
    @PostMapping("/{serverId}/maintenance/enable")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISOR')")
    public ResponseEntity<String> enableMaintenanceMode(@PathVariable Long serverId) {
        serverService.enableMaintenanceMode(serverId);
        return ResponseEntity.ok("Server set to MAINTENANCE and cron jobs deactivated.");
    }

    /**
     * ✅ Réactive un serveur (fin du mode maintenance) et réactive ses cron jobs
     */
    @PostMapping("/{serverId}/maintenance/disable")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_SUPERVISOR')")

    public ResponseEntity<String> disableMaintenanceMode(@PathVariable Long serverId) {
        serverService.disableMaintenanceMode(serverId);
        return ResponseEntity.ok("Server reactivated and cron jobs enabled.");
    }

    @GetMapping("/all")
    public List<ServerDto> getAllServers() {
        return serverService.getAllServersWithStats();
    }
}
