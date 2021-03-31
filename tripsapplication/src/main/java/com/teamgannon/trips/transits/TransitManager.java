package com.teamgannon.trips.transits;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.RouteUpdaterListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Slf4j
public class TransitManager {


    /**
     * lookup for transits
     */
    private final Map<String, TransitRoute> transitRouteMap = new HashMap<>();

    /**
     * used to track the current rout list
     */
    private final List<TransitRoute> currentRouteList = new ArrayList<>();

    /**
     * the transit map which is associated with a specific id
     */
    private Map<UUID, TransitRouteVisibilityGroup> transitMap = new HashMap<>();

    /**
     * the graphical element controlling transits
     */
    private final @NotNull Group transitGroup;

    /**
     * the label display
     */
    private final Group labelDisplayGroup = new Group();

    private final Map<Node, Label> shapeToLabel = new HashMap<>();

    private final SubScene subScene;

    /**
     * the listener to create routes on demand
     */
    private final RouteUpdaterListener routeUpdaterListener;
    private final TripsContext tripsContext;
    private final InterstellarSpacePane interstellarSpacePane;

    /**
     * whether the transits are visible or not
     */
    private boolean transitsOn;

    /**
     * the route descriptor
     */
    private @Nullable RouteDescriptor routeDescriptor;

    /**
     * used to track an active routing effort
     */
    private boolean routingActive = false;

    /**
     * current dataset
     */
    private DataSetDescriptor dataSetDescriptor;

    private boolean transitsLengthsOn = true;
    private double controlPaneOffset;


    ////////////////

    /**
     * constructor
     */
    public TransitManager(@NotNull Group world,
                          @NotNull Group sceneRoot,
                          SubScene subScene,
                          InterstellarSpacePane interstellarSpacePane,
                          RouteUpdaterListener routeUpdaterListener,
                          TripsContext tripsContext) {
        this.subScene = subScene;
        this.interstellarSpacePane = interstellarSpacePane;

        // our graphics world
        this.routeUpdaterListener = routeUpdaterListener;
        this.tripsContext = tripsContext;
        transitGroup = new Group();
        world.getChildren().add(transitGroup);
        sceneRoot.getChildren().add(labelDisplayGroup);

    }


    /**
     * finds all the transits for stars in view
     *
     * @param transitDefinitions the distance range selected
     * @param starsInView        the stars in the current plot
     */
    public void findTransits(TransitDefinitions transitDefinitions, @NotNull List<StarDisplayRecord> starsInView) {
        // clear existing
        clearTransits();

        // set
        transitsLengthsOn = true;
        transitsOn = true;
        List<TransitRangeDef> transitRangeDefList = transitDefinitions.getTransitRangeDefs();

        for (TransitRangeDef transitRangeDef : transitRangeDefList) {
            if (transitRangeDef.isEnabled()) {
                // create a transit visibilty group
                TransitRouteVisibilityGroup visibilityGroup = new TransitRouteVisibilityGroup(subScene, interstellarSpacePane,
                        controlPaneOffset, transitRangeDef, routeUpdaterListener, tripsContext);

                // plot the visibility group
                visibilityGroup.plotTransit(transitRangeDef, starsInView);

                // install it
                installGroup(visibilityGroup);
            }
        }
        updateLabels(interstellarSpacePane);

        log.info("done transits");
    }

    /**
     * install the visibility group
     *
     * @param visibilityGroup the visibility group
     */
    private void installGroup(TransitRouteVisibilityGroup visibilityGroup) {

        // map visibility group by id
        transitMap.put(visibilityGroup.getGroupId(), visibilityGroup);

        // add this visibility group to the overall transit group
        transitGroup.getChildren().add(visibilityGroup.getGroup());

        // add the internal label group to the larger group
        labelDisplayGroup.getChildren().add(visibilityGroup.getLabelGroup());
    }


    /**
     * uninstall the visibility group
     *
     * @param visibilityGroup the visibility group to uninstall
     */
    private void uninstallGroup(TransitRouteVisibilityGroup visibilityGroup) {

        // add this visibility group to the overall transit group
        transitGroup.getChildren().remove(visibilityGroup.getGroup());

        // add the internal label group to the larger group
        labelDisplayGroup.getChildren().remove(visibilityGroup.getLabelGroup());

        visibilityGroup.clear();
    }

    /**
     * used to add an offset for the screen
     *
     * @param controlPaneOffset the offset from the top to properly display this
     */
    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
    }

    /**
     * show a specific transit
     *
     * @param bandId the transit id
     * @param show   true is shwo, false is hide
     */
    public void showTransit(UUID bandId, boolean show) {
        // transit
        log.info("Transit:{} is {}", bandId, show);
        // pull the associated group
        TransitRouteVisibilityGroup visibilityGroup = transitMap.get(bandId);
        // set it visible or not based on the status
        if (visibilityGroup != null) {
            visibilityGroup.toggleTransit(show);
        }
    }

    /**
     * show the labels
     *
     * @param bandId the group id
     * @param show   true is to show labels
     */
    public void showLabels(UUID bandId, boolean show) {
        // transit
        log.info("Transit:{} is {}", bandId, show);
        // pull the associated group
        TransitRouteVisibilityGroup visibilityGroup = transitMap.get(bandId);
        // set it visible or not based on the status
        if (visibilityGroup != null) {
            visibilityGroup.toggleLabels(show);
        }
    }

    /**
     * Is transit container group visible
     *
     * @return true is yes
     */
    public boolean isVisible() {
        return transitsOn;
    }

    public void setVisible(boolean transitsOn) {
        this.transitsOn = transitsOn;
        this.transitsLengthsOn = transitsOn;
        transitGroup.setVisible(transitsOn);
        labelDisplayGroup.setVisible(transitsLengthsOn);
    }

    /**
     * set dataset descriptor context
     *
     * @param dataSetDescriptor the dataset descriptor
     */
    public void setDatasetContext(DataSetDescriptor dataSetDescriptor) {
        this.dataSetDescriptor = dataSetDescriptor;
    }

    /**
     * clears all transits
     */
    public void clearTransits() {
        transitGroup.getChildren().clear();
        transitRouteMap.clear();
        for (TransitRouteVisibilityGroup transitGroup : transitMap.values()) {
            if (transitGroup != null) {
                uninstallGroup(transitGroup);
            }
        }
        transitMap.clear();
        transitsOn = false;
        labelDisplayGroup.getChildren().clear();
        transitsLengthsOn = false;

        routeDescriptor = null;
        currentRouteList.clear();
        routingActive = false;
    }

    /**
     * toggle transit lengths
     *
     * @param transitsLengthsOn toggle the transit lengths
     */
    public void toggleTransitLengths(boolean transitsLengthsOn) {
        this.transitsLengthsOn = transitsLengthsOn;
        log.info("transit labels visibility:{}", transitsLengthsOn);
        labelDisplayGroup.setVisible(transitsLengthsOn);
        for (TransitRouteVisibilityGroup visibilityGroup : transitMap.values()) {
            visibilityGroup.toggleLabels(transitsLengthsOn);
        }
    }

    /**
     * iterate over the labels to redisplay
     *
     * @param interstellarSpacePane the interstellar pane
     */
    public void updateLabels(InterstellarSpacePane interstellarSpacePane) {
        for (TransitRouteVisibilityGroup visibilityGroup : transitMap.values()) {
            visibilityGroup.updateLabels(subScene, controlPaneOffset,
                    interstellarSpacePane.getWidth(), interstellarSpacePane.getHeight(),
                    interstellarSpacePane.getBoundsInParent());
        }

    }

}
