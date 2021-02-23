package com.teamgannon.trips.service.export;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExportResults {

    private boolean success;

    private String message;

}
