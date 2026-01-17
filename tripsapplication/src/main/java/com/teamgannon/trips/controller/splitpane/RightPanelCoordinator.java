package com.teamgannon.trips.controller.splitpane;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import com.teamgannon.trips.service.DatasetService;
import com.teamgannon.trips.service.SolarSystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class RightPanelCoordinator {

    private final RightPanelController rightPanelController;
    private final DataSetPanelCoordinator dataSetPanelCoordinator;
    private final DatasetService datasetService;
    private final SolarSystemService solarSystemService;
    private final ApplicationEventPublisher eventPublisher;
    private final SearchContextCoordinator searchContextCoordinator;

    public RightPanelCoordinator(RightPanelController rightPanelController,
                                 DataSetPanelCoordinator dataSetPanelCoordinator,
                                 DatasetService datasetService,
                                 SolarSystemService solarSystemService,
                                 ApplicationEventPublisher eventPublisher,
                                 SearchContextCoordinator searchContextCoordinator) {
        this.rightPanelController = rightPanelController;
        this.dataSetPanelCoordinator = dataSetPanelCoordinator;
        this.datasetService = datasetService;
        this.solarSystemService = solarSystemService;
        this.eventPublisher = eventPublisher;
        this.searchContextCoordinator = searchContextCoordinator;
    }

    public void initialize() {
        dataSetPanelCoordinator.loadDataSets();
        rightPanelController.setupDataSetView(datasetService, eventPublisher, searchContextCoordinator);
        rightPanelController.setupObjectViewPane();
        rightPanelController.refreshPlanetarySystems();
        log.info("Right panel initialized");
    }

    public void refreshDataSets() {
        dataSetPanelCoordinator.refreshDataSets();
        rightPanelController.refreshDataSets(datasetService);
        rightPanelController.refreshPlanetarySystems();
    }

    public void addDataSetToContext(DataSetDescriptor descriptor) {
        dataSetPanelCoordinator.addDataSet(descriptor);
    }

    public void removeDataSetFromContext(DataSetDescriptor descriptor) {
        dataSetPanelCoordinator.removeDataSet(descriptor);
    }

    public void handleDataSetAdded(DataSetDescriptor descriptor) {
        dataSetPanelCoordinator.addDataSet(descriptor);
        refreshDataSets();
    }

    public void handleDataSetRemoved(DataSetDescriptor descriptor) {
        dataSetPanelCoordinator.removeDataSet(descriptor);
        refreshDataSets();
    }

    /**
     * Switch to the interstellar side pane (default view).
     */
    public void switchToInterstellar() {
        rightPanelController.showInterstellarSidePane();
        log.info("Switched side pane to interstellar view");
    }

    /**
     * Switch to the solar system side pane.
     *
     * @param star the star to display solar system for
     */
    public void switchToSolarSystem(StarDisplayRecord star) {
        if (star == null) {
            log.warn("Cannot switch to solar system view: star is null");
            return;
        }

        // Get the solar system description
        SolarSystemDescription system = solarSystemService.getSolarSystem(star);

        // Update the side pane
        rightPanelController.showSolarSystemSidePane(system);
        log.info("Switched side pane to solar system view for: {}", star.getStarName());
    }

    /**
     * Switch to the planetary side pane.
     *
     * @param context the planetary viewing context
     */
    public void switchToPlanetary(PlanetaryContext context) {
        if (context == null) {
            log.warn("Cannot switch to planetary view: context is null");
            return;
        }

        // Update the side pane
        rightPanelController.showPlanetarySidePane(context);
        log.info("Switched side pane to planetary view for: {}", context.getPlanetName());
    }

    public void updatePlanetaryBrightestStars(List<com.teamgannon.trips.planetary.rendering.PlanetarySkyRenderer.BrightStarEntry> stars) {
        rightPanelController.updatePlanetaryBrightestStars(stars);
    }

    public void updatePlanetaryBrightestStars(List<com.teamgannon.trips.planetary.rendering.PlanetarySkyRenderer.BrightStarEntry> stars,
                                              int visibleStarCount) {
        rightPanelController.updatePlanetaryBrightestStars(stars, visibleStarCount);
    }

    public com.teamgannon.trips.screenobjects.planetary.PlanetaryViewControlPane getPlanetaryViewControlPane() {
        return rightPanelController.getPlanetarySidePane().getViewControlPane();
    }

    /**
     * Get the planetary side pane for direct access to its components.
     */
    public com.teamgannon.trips.screenobjects.planetary.PlanetarySidePane getPlanetarySidePane() {
        return rightPanelController.getPlanetarySidePane();
    }
}
