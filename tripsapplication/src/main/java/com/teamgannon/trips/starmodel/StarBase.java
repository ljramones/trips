package com.teamgannon.trips.starmodel;

import com.teamgannon.trips.algorithms.StarMath;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import javafx.scene.paint.Color;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * In memory database for star records
 * <p>
 * Created by larrymitchell on 2017-02-20.
 */
@Slf4j
@Data
public class StarBase {

    private ColorPalette colorPalette;

    private Map<UUID, AstrographicObject> database = new HashMap<>();

    public void setRecords(List<AstrographicObject> astrographicObjectList, ColorPalette colorPalette) {
        this.colorPalette = colorPalette;

        database.clear();

        for (AstrographicObject astrographicObject : astrographicObjectList) {
            database.put(astrographicObject.getId(), astrographicObject);
        }
    }

    /**
     * get a record
     *
     * @param recordId the record number
     * @return the record
     */
    public AstrographicObject getRecord(UUID recordId) {
        return database.get(recordId);
    }

    public Map<String, String> getRecordFields(UUID recordId) {
        AstrographicObject record = getRecord(recordId);
        Map<String, String> properties = new HashMap<>();

        properties.put("Star Name", record.getDisplayName());

        properties.put("Coordinates", showCoordinates(record.getCoordinates()));
        properties.put("Distance", Double.toString(record.getDistance()));
        properties.put("Right Ascension", Double.toString(record.getRa()));
        properties.put("Declination", Double.toString(record.getDeclination()));

        properties.put("Spectral Class", record.getSpectralClass());
        properties.put("Notes", record.getNotes());

        properties.put("Radius",
                String.format("%1$,.2f", record.getRadius()));

        return properties;
    }

    public DistanceReport getDistanceReport(StarDisplayRecord starDisplayRecord) {
        DistanceReport report = new DistanceReport();
        for (AstrographicObject astrographicObject : database.values()) {
            try {
                DistanceToFrom distanceToFrom = new DistanceToFrom();
                distanceToFrom.setStarFrom(starDisplayRecord.getStarName());
                distanceToFrom.setStarTo(astrographicObject.getDisplayName());
                double distance = StarMath.getDistance(starDisplayRecord.getActualCoordinates(), astrographicObject.getCoordinates());
                distanceToFrom.setDistance(distance);
                report.addDistanceToFrom(distanceToFrom);
            } catch (Exception e) {
                log.error("Failed to calculate distance:" + e);
            }
        }

        return report;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * format the coordinates for display
     *
     * @param coordinates the XYZ coordinates
     * @return a formatted string
     */
    private String showCoordinates(double[] coordinates) {
        return coordinates[0] + ", " + coordinates[1] + ", " + coordinates[2];
    }

    /**
     * convert the color
     *
     * @param colors the raw color values
     * @return the color
     */
    private Color getColor(double[] colors) {
        return Color.color(colors[0], colors[1], colors[2]);
    }

}
