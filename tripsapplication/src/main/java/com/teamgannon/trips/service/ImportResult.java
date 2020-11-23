package com.teamgannon.trips.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImportResult {

    private boolean success;

    private String message;

}
