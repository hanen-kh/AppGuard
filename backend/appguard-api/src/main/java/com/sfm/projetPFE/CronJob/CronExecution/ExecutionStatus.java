package com.sfm.projetPFE.CronJob.CronExecution;

import java.util.Arrays;

public enum ExecutionStatus {
    RUNNING("Running"),
    SUCCESS("Success"),
    FAILURE("Failed"), // Using "Failed" to align with common terminology
    TIMEOUT("Timed Out"),
    CANCELED("Canceled"),
    NOT_EXECUTED("Not Executed"),
    AGENT_UNREACHABLE("AGENT_UNREACHABLE");
    private final String displayName;

    ExecutionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Optional: For deserialization or input validation
    public static ExecutionStatus fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(status -> status.displayName.equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown display name: " + displayName));
    }
}
