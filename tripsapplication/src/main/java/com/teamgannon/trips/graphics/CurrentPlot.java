package com.teamgannon.trips.graphics;

import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CurrentPlot {

    /**
     * the dataset descriptor for this plot
     */
    private DataSetDescriptor dataSetDescriptor;

    /**
     * the center coordinates for this plot
     */
    private double[] centerCoordinates;

    /**
     * th center star
     */
    private String centerStar;

    /**
     * the star display preferences
     */
    private StarDisplayPreferences starDisplayPreferences;

    /**
     * the polities
     */
    private CivilizationDisplayPreferences civilizationDisplayPreferences;

    /**
     * the list of stars
     */
    private List<StarDisplayRecord> starDisplayRecordList = new ArrayList<>();

    /**
     * the color palette
     */
    private ColorPalette colorPalette;

    /**
     * add a record
     *
     * @param record the record
     */
    public void addRecord(StarDisplayRecord record) {
        starDisplayRecordList.add(record);
    }

}
