package com.teamgannon.trips.transits;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import javafx.scene.Group;
import javafx.scene.SubScene;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Manages transit visualization across all transit bands.
 * Coordinates TransitRouteVisibilityGroup instances and their scene graph placement.
 */
@Slf4j
@Component
public class TransitManager {

    private final Map<UUID, TransitRouteVisibilityGroup> transitMap = new HashMap<>();

    private Group transitGroup;
    private final Group labelDisplayGroup = new Group();

    private final TripsContext tripsContext;
    private final TransitCalculatorFactory calculatorFactory;
    private final TransitRouteBuilderService routeBuilderService;

    // Graphics state for building context
    private SubScene subScene;
    private InterstellarSpacePane interstellarSpacePane;
    private double controlPaneOffset;

    public TransitManager(TripsContext tripsContext,
                          TransitCalculatorFactory calculatorFactory,
                          TransitRouteBuilderService routeBuilderService) {
        this.tripsContext = tripsContext;
        this.calculatorFactory = calculatorFactory;
        this.routeBuilderService = routeBuilderService;
    }

    /**
     * Initialize graphics references. Must be called before using transit features.
     */
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
     * Set the control pane offset for label positioning.
     */
    public void setControlPaneOffset(double controlPaneOffset) {
        this.controlPaneOffset = controlPaneOffset;
    }

    /**
     * Set the current dataset for route building.
     */
    public void setDataSetDescriptor(DataSetDescriptor descriptor) {
        routeBuilderService.setDataSetDescriptor(descriptor);
    }

    /**
     * Build the graphics context for creating visibility groups.
     *
     * @param starCount the number of stars being processed (used to select optimal algorithm)
     */
    private TransitGraphicsContext buildContext(int starCount) {
        ITransitDistanceCalculator calculator = calculatorFactory.getCalculator(starCount);
        return TransitGraphicsContext.builder()
                .subScene(subScene)
                .interstellarSpacePane(interstellarSpacePane)
                .controlPaneOffset(controlPaneOffset)
                .distanceCalculator(calculator)
                .routeBuilderService(routeBuilderService)
                .tripsContext(tripsContext)
                .build();
    }

    /**
     * Find and display transits for stars in view.
     * <p>
     * Automatically selects the optimal algorithm based on star count:
     * <ul>
     *   <li>≤ 100 stars: O(n²) brute-force (lower overhead)</li>
     *   <li>> 100 stars: O(n log n) KD-Tree with parallel queries</li>
     * </ul>
     */
    public void findTransits(TransitDefinitions transitDefinitions, @NotNull List<StarDisplayRecord> starsInView) {
        clearTransits();

        log.debug("Finding transits for {} stars", starsInView.size());
        TransitGraphicsContext context = buildContext(starsInView.size());
        List<TransitRangeDef> transitRangeDefList = transitDefinitions.getTransitRangeDefs();

        for (TransitRangeDef transitRangeDef : transitRangeDefList) {
            if (transitRangeDef.isEnabled()) {
                TransitRouteVisibilityGroup visibilityGroup = new TransitRouteVisibilityGroup(context, transitRangeDef);
                visibilityGroup.plotTransit(transitRangeDef, starsInView);
                installGroup(visibilityGroup);
            }
        }

        updateLabels();
        log.debug("Transits computed and displayed");
    }

    private void installGroup(TransitRouteVisibilityGroup visibilityGroup) {
        transitMap.put(visibilityGroup.getGroupId(), visibilityGroup);
        transitGroup.getChildren().add(visibilityGroup.getGroup());
        labelDisplayGroup.getChildren().add(visibilityGroup.getLabelGroup());
    }

    private void uninstallGroup(TransitRouteVisibilityGroup visibilityGroup) {
        transitGroup.getChildren().remove(visibilityGroup.getGroup());
        labelDisplayGroup.getChildren().remove(visibilityGroup.getLabelGroup());
        visibilityGroup.clear();
    }

    /**
     * Show or hide a specific transit band.
     */
    public void showTransit(UUID bandId, boolean show) {
        log.debug("Transit band {} visibility: {}", bandId, show);
        TransitRouteVisibilityGroup visibilityGroup = transitMap.get(bandId);
        if (visibilityGroup != null) {
            visibilityGroup.toggleTransit(show);
        }
    }

    /**
     * Show or hide labels for a specific transit band.
     */
    public void showLabels(UUID bandId, boolean show) {
        log.debug("Transit band {} labels: {}", bandId, show);
        TransitRouteVisibilityGroup visibilityGroup = transitMap.get(bandId);
        if (visibilityGroup != null) {
            visibilityGroup.toggleLabels(show);
        }
    }

    /**
     * Check if transits are currently visible.
     */
    public boolean isVisible() {
        return transitGroup != null && transitGroup.isVisible();
    }

    /**
     * Set visibility of all transits and labels.
     */
    public void setVisible(boolean visible) {
        transitGroup.setVisible(visible);
        labelDisplayGroup.setVisible(visible);
    }

    /**
     * Clear all transits from the display.
     */
    public void clearTransits() {
        transitGroup.getChildren().clear();
        for (TransitRouteVisibilityGroup group : transitMap.values()) {
            if (group != null) {
                uninstallGroup(group);
            }
        }
        transitMap.clear();
        labelDisplayGroup.getChildren().clear();
    }

    /**
     * Toggle visibility of all transit labels.
     */
    public void toggleTransitLengths(boolean showLabels) {
        log.debug("Transit labels visibility: {}", showLabels);
        labelDisplayGroup.setVisible(showLabels);
        for (TransitRouteVisibilityGroup visibilityGroup : transitMap.values()) {
            visibilityGroup.toggleLabels(showLabels);
        }
    }

    /**
     * Update all label positions (call after rotation/zoom).
     */
    public void updateLabels() {
        for (TransitRouteVisibilityGroup visibilityGroup : transitMap.values()) {
            visibilityGroup.updateLabels();
        }
    }
}
