package com.sfm.projetPFE.CronJob.CronExecution;

import com.sfm.projetPFE.CronJob.Cron.CronJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/job-executions")
@RequiredArgsConstructor
public class CronExecutionController {

    @Autowired
    private CronExecutionServiceImpl cronExecutionService;

    @Autowired
    private CronJobRepository cronJobRepository;


    @GetMapping("/executions")
    public List<CronExecutionDtos.CronExecutionDto> getAllExecutions() {
        return cronExecutionService.getAllExecutions();
    }
}
