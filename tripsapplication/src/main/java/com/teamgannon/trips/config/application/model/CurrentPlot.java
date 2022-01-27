package com.teamgannon.trips.config.application.model;

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
     * the center star
     */
    private String centerStar;

    /**
     * the list of stars
     */
    private @NotNull List<StarDisplayRecord> starDisplayRecordList = new ArrayList<>();

    /**
     * the color palette
     */
    private ColorPalette colorPalette;

    /**
     * a map from the full route to the partial routes associated with it
     */
    private Map<UUID, RouteDescriptor> routeMapping = new HashMap<>();

    /**
     * civilization preferences from DB
     */
    private CivilizationDisplayPreferences civilizationDisplayPreferences;

    /**
     * star display preferences from DB
     */
    private StarDisplayPreferences starDisplayPreferences;


    /**
     * setup the mechanics of the plot
     *
     * @param dataSetDescriptor the dataset descriptor that this plot belongs to
     * @param centerCoordinates the center x,y,z coordinates of the plot
     * @param centerStar        the center star of the plot
     * @param colorPalette      the color palette
     */
    public void setupPlot(DataSetDescriptor dataSetDescriptor,
                          double[] centerCoordinates,
                          String centerStar,
                          ColorPalette colorPalette) {

        this.dataSetDescriptor = dataSetDescriptor;
        this.centerCoordinates = centerCoordinates;
        this.centerStar = centerStar;
        this.colorPalette = colorPalette;
    }

    public void setCivilizationDisplayPreferences(CivilizationDisplayPreferences civilizationDisplayPreferences) {
        this.civilizationDisplayPreferences = civilizationDisplayPreferences;
    }

    public void setStarDisplayPreferences(StarDisplayPreferences starDisplayPreferences) {
        this.starDisplayPreferences = starDisplayPreferences;
    }

    /**
     * clear the plot
     */
    public void clearPlot() {
        starDisplayRecordList.clear();
        starToLabelLookup.clear();
        starLookup.clear();
        plotActive = false;
        centerCoordinates = new double[3];
        clearRoutes();
    }

    ////////////////  route management   //////////////////////

    /**
     * add a route to the plot
     *
     * @param routeDescriptor the route descriptor to add
     */
    public void addRoute(UUID parent, RouteDescriptor routeDescriptor) {
        log.info("\nAdd route with Id={}\n", routeDescriptor.getId());
        if (!routeMapping.containsKey(routeDescriptor.getId())) {
            routeMapping.put(routeDescriptor.getId(), routeDescriptor);
        }
    }

    public void addRoutes(UUID parent, List<RouteDescriptor> routeDescriptorList) {
        for (RouteDescriptor routeDescriptor : routeDescriptorList) {
            addRoute(parent, routeDescriptor);
        }
    }


    /**
     * remove a route from the plot
     *
     * @param routeDescriptor the route descriptor to remove
     */
    public void removeRoute(RouteDescriptor routeDescriptor) {
        log.info("\nRemoved route with Id={}\n", routeDescriptor.getId());
        routeMapping.remove(routeDescriptor.getId());
    }

    /**
     * get a route based on id
     *
     * @param id the id to look up
     * @return the route descriptor or null if not found
     */
    public RouteDescriptor getRoute(UUID id) {
        return routeMapping.get(id);
    }

    /**
     * get all the routes as a List
     *
     * @return the routes
     */
    public List<RouteDescriptor> getRoutes() {
        return new ArrayList<>(routeMapping.values());
    }

    /**
     * clear the routes
     */
    public void clearRoutes() {
        routeMapping.clear();
    }

    /**
     * get the map showing which routes are visible
     *
     * @return the route visibility map
     */
    public Map<UUID, RouteVisibility> getVisibilityMap() {
        Map<UUID, RouteVisibility> visibilityMap = new HashMap<>();
        for (UUID id : routeMapping.keySet()) {
            RouteDescriptor routeDescriptor = routeMapping.get(id);
            visibilityMap.put(id, routeDescriptor.getVisibility());
        }
        return visibilityMap;
    }

    //////////////////// star records  ///////////////////

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
     * retrieve a star
     *
     * @param starId the guid for the star
     * @return the star
     */
    public Node getStar(UUID starId) {
        return starLookup.get(starId);
    }

    /**
     * add a star
     *
     * @param id   the id of the star
     * @param star the star
     */
    public void addStar(UUID id, Node star) {
        starLookup.put(id, star);
    }

    /**
     * get the ids of the star
     *
     * @return the set of ids
     */
    public @NotNull Set<UUID> getStarIds() {
        return starLookup.keySet();
    }

    /**
     * check if a star is present and visible on the plot
     *
     * @param id the star id
     * @return true is present and visible
     */
    public boolean isStarVisible(UUID id) {
        return starLookup.containsKey(id);
    }

    ////////////////// label management  //////////////////

    /**
     * map a label to a star
     *
     * @param starId    the star id
     * @param starLabel the label for the star
     */
    public void mapLabelToStar(UUID starId, Label starLabel) {
        starToLabelLookup.put(starId, starLabel);
    }

    /**
     * get a label for the star
     *
     * @param starId the star id
     * @return the label
     */
    public Label getLabelForStar(UUID starId) {
        return starToLabelLookup.get(starId);
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

}
