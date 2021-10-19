package com.teamgannon.trips.config.application;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Slf4j
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
     * the List of current routes and whether they are visible or not
     */
    private Map<UUID, RouteDescriptor> routeDescriptorMap = new HashMap<>();


    /**
     * add a record
     *
     * @param record the record
     */
    public void addRecord(StarDisplayRecord record) {

        // center star is allows labeled
        if (record.isCenter()) {
            record.setDisplayLabel(true);
        }
        if (record.isLabelForced()) {
            record.setCurrentLabelDisplayScore(1000);
        }
        starDisplayRecordList.add(record);
        // put star display record into the label sort
    }

    /**
     * add a route to the plot
     *
     * @param routeDescriptor the route descriptor to add
     */
    public void addRoute(RouteDescriptor routeDescriptor) {
        routeDescriptorMap.put(routeDescriptor.getId(), routeDescriptor);
    }

    /**
     * remove a route from the plot
     *
     * @param routeDescriptor the route descriptor to remove
     */
    public void removeRoute(RouteDescriptor routeDescriptor) {
        routeDescriptorMap.remove(routeDescriptor.getId());
    }

    /**
     * get a reoute based on id
     *
     * @param id the id to look up
     * @return the route descriptor or null if not found
     */
    public RouteDescriptor getRoute(UUID id) {
        return routeDescriptorMap.get(id);
    }

    /**
     * get all the routes as a List
     *
     * @return the routes
     */
    public List<RouteDescriptor> getRoutes() {
        return new ArrayList<>(routeDescriptorMap.values());
    }

    public Map<UUID, RouteVisibility> getVisibilityMap() {
        Map<UUID, RouteVisibility> visibilityMap = new HashMap<>();
        routeDescriptorMap.forEach((key, value) -> visibilityMap.put(key, value.getVisibility()));
        return visibilityMap;
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
        for (int i = 0; i < labelCount; i++) {
            StarDisplayRecord starDisplayRecord = starDisplayRecordList.get(i);
            starDisplayRecord.setDisplayLabel(true);
        }

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

    /**
     * check if a star is present and visible on the plot
     *
     * @param id the star id
     * @return triue is present and visible
     */
    public boolean isStarVisible(UUID id) {
        return starLookup.containsKey(id);
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
