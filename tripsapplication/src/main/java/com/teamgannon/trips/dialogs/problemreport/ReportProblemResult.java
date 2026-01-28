package com.teamgannon.trips.dialogs.problemreport;

import lombok.Builder;
import lombok.Data;

/**
 * Result from the Report Problem dialog.
 */
@Data
@Builder
public class ReportProblemResult {

    private boolean submitted;
    private String description;
    private boolean includeScreenshot;
    private boolean includeLogs;
    private boolean includeSystemInfo;
    private boolean includeCrashFiles;

    /**
     * Creates a cancelled result.
     */
    public static ReportProblemResult cancelled() {
        return ReportProblemResult.builder()
                .submitted(false)
                .build();
    }
}
