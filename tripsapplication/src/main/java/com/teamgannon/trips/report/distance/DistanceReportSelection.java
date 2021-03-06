package com.teamgannon.trips.report.distance;


import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistanceReportSelection {

    @Builder.Default
    private boolean selected = false;

    private StarDisplayRecord record;

}
