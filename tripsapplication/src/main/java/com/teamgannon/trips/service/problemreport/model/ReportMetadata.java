package com.teamgannon.trips.service.problemreport.model;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * Metadata for a problem report, serialized to report.json in the ZIP bundle.
 */
@Data
@Builder
public class ReportMetadata {

    private String reportId;
    private String installId;
    private String email;
    private String displayName;
    private String appVersion;
    private Instant timestamp;
    private String description;
    private Attachments attachments;
    private Platform platform;

    @Data
    @Builder
    public static class Attachments {
        private boolean hasScreenshot;
        private boolean hasLogTail;
        private boolean hasSystemInfo;
        private boolean hasCrashFiles;
    }

    @Data
    @Builder
    public static class Platform {
        private String os;
        private String osVersion;
        private String javaVersion;
        private String javafxVersion;
    }
}
