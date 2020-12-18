package com.teamgannon.trips.report.distance;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Data
public class DistanceReport {

    /**
     * whether we measure or not
     */
    private boolean saveSelected;

    /**
     * out source star to preform measurements
     */
    private StarDisplayRecord sourceStar;

    /**
     * the report
     */
    private String generatedReport;

    /**
     * the list of distance measures
     */
    private @NotNull List<StarDistanceMeasure> distances = new ArrayList<>();

    /**
     * constructor
     *
     * @param starDisplayRecord the star to calculate form
     */
    public DistanceReport(StarDisplayRecord starDisplayRecord) {
        this.sourceStar = starDisplayRecord;
    }

    /**
     * find a distance value
     *
     * @param targetStar from center to the target star
     */
    public void findDistance(@NotNull StarDisplayRecord targetStar) {
        try {
            double[] source = sourceStar.getActualCoordinates();
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

    /**
     * generate the report
     * sort it in order of distance - ascending
     */
    public void generateReport() {
        distances.sort(Comparator.comparing(StarDistanceMeasure::getDistance));
        StringBuilder report = new StringBuilder("Distance Report for " + sourceStar.getStarName() + "\n\n");
        int i = 0;
        for (StarDistanceMeasure measure : distances) {
            report.append(String.format("%d: %s - %.3f light years\n", i++, measure.getStarName(), measure.getDistance()));
        }
        report.append("\nReport Complete: ").append(distances.size()).append(" stars\n\n");

        generatedReport = report.toString();
    }

    /**
     * get the generated report
     *
     * @return the report
     */
    public String getGeneratedReport() {
        return generatedReport;
    }

    /**
     * tell the recipient whether we save thi report or not
     *
     * @param save true is save
     */
    public void setSave(boolean save) {
        this.saveSelected = save;
    }

}
