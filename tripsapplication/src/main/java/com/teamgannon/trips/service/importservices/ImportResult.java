package com.teamgannon.trips.service.importservices;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportResult {

    private boolean success;

    private String message;

}
