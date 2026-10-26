package com.sfm.pfe.agent_cron.Controller;

import com.sfm.pfe.agent_cron.DTOs.CommandRequest;
import com.sfm.pfe.agent_cron.DTOs.ExecutionResponse;
import com.sfm.pfe.agent_cron.Services.ExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class ExecutionController {

    @Autowired
    private ExecutionService executionService;

    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponse> executeCommand(@RequestBody CommandRequest request) {
        ExecutionResponse response = executionService.execute(request.getCommand());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
