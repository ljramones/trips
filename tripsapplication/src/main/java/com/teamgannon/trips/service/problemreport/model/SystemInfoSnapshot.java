package com.teamgannon.trips.service.problemreport.model;

import lombok.Builder;
import lombok.Data;

/**
 * Snapshot of system information for inclusion in problem reports.
 */
@Data
@Builder
public class SystemInfoSnapshot {

    // Operating system info
    private String osName;
    private String osVersion;
    private String osArch;
    private String osManufacturer;
    private int osBitness;

    // Java runtime info
    private String javaVersion;
    private String javaVendor;
    private String javaHome;
    private String javafxVersion;

    // Hardware info
    private int physicalProcessors;
    private int logicalProcessors;
    private String processorName;
    private long totalMemoryMb;
    private long availableMemoryMb;

    // Graphics
    private String graphicsCards;

    // Current process info
    private int processId;
    private long uptimeMs;
    private int threadCount;

    // TRIPS app info
    private String appVersion;
    private String workingDirectory;
}
