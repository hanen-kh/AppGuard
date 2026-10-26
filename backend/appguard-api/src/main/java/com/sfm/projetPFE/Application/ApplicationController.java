package com.sfm.projetPFE.Application;


import com.sfm.projetPFE.LogEntry.LogEntry;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationServiceImpl applicationService;


    @PostMapping("/create")
    public ResponseEntity<Application> create(@RequestBody Application app) {
        return ResponseEntity.ok(applicationService.save(app));
    }

    @GetMapping("/all_apps")
    public ResponseEntity<List<ApplicationDto>> getAllApplications() {
        List<ApplicationDto> applications = applicationService.getAllApplications();
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getById(@PathVariable Long id) {
        return applicationService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/logs-filtered/{applicationName}")
    public List<LogEntry> getLogsFiltered(
            @PathVariable String applicationName,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        return applicationService.getLogsFiltered(applicationName, level, startDate, endDate);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Application> getByNom(@RequestParam String index) {
        return (ResponseEntity<Application>) applicationService.getByIndex(index);
    }

    @GetMapping("/by_name/{name}")
    public ResponseEntity<ApplicationDto> getApplicationDetails(@PathVariable String name) {
        ApplicationDto details = applicationService.getApplicationByName(name);
        return ResponseEntity.ok(details);
    }

    @GetMapping("/logs/{appName}")
    public ResponseEntity<List<LogEntry>> getLogsByAppName(@PathVariable String appName) {
        List<LogEntry> logs = applicationService.getLogsByApplicationName(appName);
        return ResponseEntity.ok(logs);
    }
}

