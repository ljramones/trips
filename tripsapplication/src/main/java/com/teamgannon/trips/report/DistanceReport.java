package com.teamgannon.trips.report;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Data
public class DistanceReport {

    private boolean saveSelected;

    private StarDisplayRecord starDisplayRecord;

    private String generatedReport;

    private List<StarDistanceMeasure> distances = new ArrayList<>();

    public DistanceReport(StarDisplayRecord starDisplayRecord) {
        this.starDisplayRecord = starDisplayRecord;
    }

    public void findDistance(StarDisplayRecord targetStar) {
        try {
            double[] source = starDisplayRecord.getActualCoordinates();
            double[] target = targetStar.getActualCoordinates();
            double distance = StarMath.getDistance(source, target);
            StarDistanceMeasure starDistanceMeasure = StarDistanceMeasure
                    .builder()
                    .starId(targetStar.getRecordId())
                    .starName(targetStar.getStarName())
                    .distance(distance)
                    .build();
            distances.add(starDistanceMeasure);
        } catch (Exception e) {
            log.error("attempt to measure arrays of varying size: {}", targetStar);
        }
    }

    public void generateReport() {
        StringBuilder report = new StringBuilder("Distance Report for " + starDisplayRecord.getStarName() + "\n\n");
        int i=0;
        for (StarDistanceMeasure measure: distances) {
            report.append(String.format("%d: %s - %.3f light years\n", i++, measure.getStarName(), measure.getDistance()));
        }
        report.append("\nReport Complete: ").append(distances.size()).append(" stars\n\n");

        generatedReport = report.toString();
    }

    public String getGeneratedReport() {
        return generatedReport;
    }

    public void setSave(boolean save) {
        this.saveSelected = save;
    }

}
