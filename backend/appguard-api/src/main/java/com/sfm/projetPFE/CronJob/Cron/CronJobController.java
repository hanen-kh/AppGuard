package com.sfm.projetPFE.CronJob.Cron;


import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cron-jobs")
@AllArgsConstructor
public class CronJobController {

    @Autowired
    private CronJobServiceImpl cronJobService;

    @PostMapping("/server/{serverId}")
    public ResponseEntity<Void> createCronJob(
            @PathVariable Long serverId,
            @Valid @RequestBody CronJobDtos.CreateCronRequest request
    ) {
        cronJobService.createCronJob(serverId, request);
        return ResponseEntity.ok().build(); // ✅ Retourne 200 OK sans body
    }

    @PutMapping("/{id}")
    public ResponseEntity<CronJob> updateCronJob(
            @PathVariable Long id,
            @RequestBody CronJob updatedCronJob
    ) {
        CronJob updated = cronJobService.updateCronJob(id, updatedCronJob);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCronJob(@PathVariable Long id) {
        cronJobService.deleteCronJob(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<CronJob> getCronJob(@PathVariable Long id) {
        Optional<CronJob> job = cronJobService.getCronJob(id);
        return job.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/allcronjobs")
    public ResponseEntity<List<CronJobDtos.CronJobResponse>> getAllCronJobs() {
        List<CronJobDtos.CronJobResponse> cronJobs = cronJobService.getAllCronJobs();
        return ResponseEntity.ok(cronJobs);
    }
    /**
     * Planifie (active) un cron job.
     */
    @PostMapping("/{id}/schedule")
    public ResponseEntity<String> scheduleCronJob(@PathVariable Long id) {
        cronJobService.scheduleCronJob(id);
        return ResponseEntity.ok("Cron job scheduled successfully.");
    }
}
