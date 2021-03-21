package com.teamgannon.trips.config.application;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.IntStream;

@Data
public class CurrentPlot {

    /**
     * the lookout for drawn stars
     */
    private final Map<UUID, Node> starLookup = new HashMap<>();

    /**
     * a one way map form star id to label of the star
     */
    private final Map<UUID, Label> starToLabelLookup = new HashMap<>();

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
    private @NotNull List<StarDisplayRecord> starDisplayRecordList = new ArrayList<>();

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
        // put star display record into the label sort
    }

    /**
     * once we have all the labels, we create a sub list that has the stars that we allow labels to show
     *
     * @param labelCount the user supplied count of labels to show
     */
    public void determineVisibleLabels(int labelCount) {
        if (labelCount > starDisplayRecordList.size()) {
            labelCount = starDisplayRecordList.size();
        }

        // sort the list in order, duplicates are keep
        starDisplayRecordList.sort(Comparator.comparing(StarDisplayRecord::getCurrentLabelDisplayScore).reversed());

        // create a check set for the user count
        IntStream.range(0, labelCount).mapToObj(i -> starDisplayRecordList.get(i)).forEach(starDisplayRecord -> starDisplayRecord.setDisplayLabel(true));

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


    public @NotNull Set<UUID> getStarIds() {
        return starLookup.keySet();
    }


    public Label getLabelForStar(UUID starId) {
        return starToLabelLookup.get(starId);
    }

    public void clearPlot() {
        starDisplayRecordList.clear();
        starToLabelLookup.clear();
        starLookup.clear();
        plotActive = false;
        centerCoordinates = new double[3];
    }

    public void setupPlot(DataSetDescriptor dataSetDescriptor, double[] centerCoordinates,
                          String centerStar, ColorPalette colorPalette,
                          StarDisplayPreferences starDisplayPreferences,
                          CivilizationDisplayPreferences civilizationDisplayPreferences) {

        this.dataSetDescriptor = dataSetDescriptor;
        this.centerCoordinates = centerCoordinates;
        this.centerStar = centerStar;
        this.colorPalette = colorPalette;
        this.starDisplayPreferences = starDisplayPreferences;
        this.civilizationDisplayPreferences = civilizationDisplayPreferences;
    }
}
