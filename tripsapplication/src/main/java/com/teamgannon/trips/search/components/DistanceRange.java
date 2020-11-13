package com.teamgannon.trips.search.components;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistanceRange {

    @Builder.Default
    private double min = 0;

    @Builder.Default
    private double max = 20.0;

    @Builder.Default
    private double lowValue = 0;

    @Builder.Default
    private double highValue = 20.0;

}
