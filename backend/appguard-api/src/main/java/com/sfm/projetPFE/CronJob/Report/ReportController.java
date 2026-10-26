package com.sfm.projetPFE.CronJob.Report;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    @Autowired
    private ReportServiceImpl reportService;

    @PostMapping("/generate")
    public ResponseEntity<ReportDataDto> generateReport() {
        ReportDataDto report = reportService.generateGlobalReport();
        return ResponseEntity.ok(report);
    }



}
