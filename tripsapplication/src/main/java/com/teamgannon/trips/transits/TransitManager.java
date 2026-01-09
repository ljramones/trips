package com.teamgannon.trips.transits;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.service.measure.StarMeasurementService;
import javafx.scene.Group;
import javafx.scene.SubScene;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class TransitManager {

    /**
     * the transit map which is associated with a specific id
     */
    private final Map<UUID, TransitRouteVisibilityGroup> transitMap = new HashMap<>();

    /**
     * the graphical element controlling transits
     */
    private Group transitGroup;

    /**
     * the label display
     */
    private final Group labelDisplayGroup = new Group();

    /**
     * subscene used to display labels in a flat glass manner
     */
    private SubScene subScene;

    /**
     * event publisher for route events
     */
    private final ApplicationEventPublisher eventPublisher;
    private final TripsContext tripsContext;
    private final StarMeasurementService starMeasurementService;
    private InterstellarSpacePane interstellarSpacePane;

    /**
     * whether the transits are visible or not
     */
    private boolean transitsOn;

    private boolean transitsLengthsOn = true;
    /**
     * -- SETTER --
     *  used to add an offset for the screen
     *
     * @param controlPaneOffset the offset from the top to properly display this
     */
    @Setter
    private double controlPaneOffset;


    ////////////////

    /**
     * constructor
     */
    public TransitManager(TripsContext tripsContext,
                          StarMeasurementService starMeasurementService,
                          ApplicationEventPublisher eventPublisher) {

        // our graphics world
        this.tripsContext = tripsContext;
        this.starMeasurementService = starMeasurementService;
        this.eventPublisher = eventPublisher;
    }

    public void setGraphics(Group sceneRoot,
                            Group world,
                            SubScene subScene,
                            InterstellarSpacePane interstellarSpacePane) {

        this.interstellarSpacePane = interstellarSpacePane;

        this.subScene = subScene;
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
                TransitRouteVisibilityGroup visibilityGroup = new TransitRouteVisibilityGroup(
                        subScene, interstellarSpacePane, starMeasurementService,
                        controlPaneOffset, transitRangeDef, eventPublisher, tripsContext);

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
     * show a specific transit
     *
     * @param bandId the transit id
     * @param show   true is shwo, false is hide
     */
    public void showTransit(UUID bandId, boolean show) {
        // transit
        log.info("Link:{} is {}", bandId, show);
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
        log.info("Link:{} is {}", bandId, show);
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
     * clears all transits
     */
    public void clearTransits() {
        transitGroup.getChildren().clear();
        for (TransitRouteVisibilityGroup transitGroup : transitMap.values()) {
            if (transitGroup != null) {
                uninstallGroup(transitGroup);
            }
        }
        transitMap.clear();
        transitsOn = false;
        labelDisplayGroup.getChildren().clear();
        transitsLengthsOn = false;
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
