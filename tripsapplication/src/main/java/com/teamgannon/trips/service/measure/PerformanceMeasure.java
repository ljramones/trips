package com.teamgannon.trips.service.measure;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PerformanceMeasure {

    private int numberProcessors;

    private long numbersOfStars;

    private double distance;

    private long worseCasePaths;

    private double timeToDoRouteSearch;

    private long memorySize;
}
