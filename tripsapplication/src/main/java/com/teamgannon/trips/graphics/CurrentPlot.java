package com.teamgannon.trips.graphics;

import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.entities.Xform;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import lombok.Data;

import java.util.*;

@Data
public class CurrentPlot {

    /**
     * the dataset descriptor for this plot
     */
    private DataSetDescriptor dataSetDescriptor;

    /**
     * whether the plot is currently active
     */
    private boolean plotActive = false;

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
     * the lookout for drawn stars
     */
    private final Map<UUID, Node> starLookup = new HashMap<>();

    /**
     * a one way map form star id to label of the star
     */
    private final Map<UUID, Label> starToLabelLookup = new HashMap<>();

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

    /**
     * retrieve a star
     *
     * @param starId the guid for the star
     * @return the star
     */
    public Node getStar(UUID starId) {
        return starLookup.get(starId);
    }

    public void addStar(UUID id, Node star) {
        starLookup.put(id, star);
    }

    public void mapLabelToStar(UUID starId, Label starLabel) {
        starToLabelLookup.put(starId, starLabel);
    }


    public Set<UUID> getStarIds() {
        return starLookup.keySet();
    }

    public void clearStars() {
        starLookup.clear();
    }

    public Label getLabelForStar(UUID starId) {
        return starToLabelLookup.get(starId);
    }

}
