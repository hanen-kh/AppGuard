package com.sfm.pfe.agent_cron.Services;


import com.sfm.pfe.agent_cron.DTOs.ExecutionResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Service
public class ExecutionService {
    private static final Logger log = LoggerFactory.getLogger(ExecutionService.class);

    public ExecutionResponse execute(String command) {
        ExecutionResponse response = new ExecutionResponse();
        Process process = null;
        Instant start = Instant.now();

        try {
            String fullCommand = "/usr/bin/time -v bash -c \"" + command.replace("\"", "\\\"") + "\"";
            log.info("Executing command: {}", command);

            process = Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", fullCommand});

            // Gestion du timeout
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            Instant end = Instant.now();
            // Conversion en millisecondes
            response.setDurationMillis(Duration.between(start, end).toMillis());

            if (!finished) {
                process.destroyForcibly();
                return buildTimeoutResponse(response);
            }

            // Traitement des résultats
            response.setExitCode(process.exitValue());
            response.setSuccess(response.getExitCode() == 0);

            String output = readStream(process.getInputStream());
            String errorOutput = readStream(process.getErrorStream());

            // Extraction des métriques
            extractAndSetMetrics(response, errorOutput);

            // Ne garder que les vraies erreurs
            response.setErrorOutput(filterRealErrors(errorOutput));

            response.setOutput(output);
        } catch (Exception e) {
            log.error("Command execution failed", e);
            return buildErrorResponse(response, e.getMessage());
        }

        return response;
    }

    // Méthodes helper manquantes
    private ExecutionResponse buildTimeoutResponse(ExecutionResponse response) {
        response.setSuccess(false);
        response.setExitCode(-1);
        response.setErrorOutput("Execution timeout exceeded (60s)");
        return response;
    }

    private ExecutionResponse buildErrorResponse(ExecutionResponse response, String errorMessage) {
        response.setSuccess(false);
        response.setExitCode(-1);
        response.setErrorOutput("System error: " + errorMessage);
        return response;
    }

    private String readStream(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            log.warn("Stream reading failed", e);
            return "";
        }
    }

    private void extractAndSetMetrics(ExecutionResponse response, String errorOutput) {
        // Extraction des métriques sans les inclure dans la réponse
        AtomicReference<Double> cpuTime = new AtomicReference<>(0.0);
        AtomicReference<Long> memoryUsage = new AtomicReference<>(0L);

        errorOutput.lines().forEach(line -> {
            if (line.contains("User time (seconds):")) {
                cpuTime.set(parseDouble(line.split(":")[1].trim()));
            } else if (line.contains("Maximum resident set size")) {
                memoryUsage.set(parseLong(line.split(":")[1].trim()));
            }
        });

        response.setCpuUsagePercent(cpuTime.get() * 100);
        response.setMemoryUsedKB(memoryUsage.get());
    }

    private String filterRealErrors(String errorOutput) {
        return errorOutput.lines()
                .filter(line -> !line.startsWith("\t"))
                .filter(line -> !line.contains("Command being timed:"))
                .collect(Collectors.joining("\n"));
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}