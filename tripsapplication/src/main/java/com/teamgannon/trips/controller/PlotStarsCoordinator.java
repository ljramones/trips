package com.teamgannon.trips.controller;

import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.controller.splitpane.SearchContextCoordinator;
import com.teamgannon.trips.events.PlotStarsEvent;
import com.teamgannon.trips.events.StatusUpdateEvent;
import com.teamgannon.trips.graphics.PlotManager;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.jpa.model.CivilizationDisplayPreferences;
import com.teamgannon.trips.routing.sidepanel.RoutingPanel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Phase 7.14 / Issue 57: business-logic side of the
 * {@link PlotStarsEvent} handler that used to live inline inside
 * {@code MainSplitPaneManager}.
 * <p>
 * The {@code @EventListener} method stays on {@code MainSplitPaneManager}
 * (Spring discovers it there and it stays close to the other split-pane
 * event wiring), but the actual work — validating the request, computing
 * geometry, calling the plot pipeline, refreshing the routing panel,
 * publishing the status update — moves here so it can be unit-tested
 * without TestFX.
 *
 * <h2>Why a separate component?</h2>
 * Previously the listener method was ~50 lines of inline orchestration
 * + try/catch + FxThread plumbing, with two private helper methods
 * ({@link #calculateCenterCoordinates} and {@link #calculateDisplayRadius})
 * for geometry. Hard to test without spinning up the whole controller.
 * Pulled out, the coordinator has clear inputs (the event + injected
 * services) and no scene-graph dependency.
 *
 * <h2>FX thread contract</h2>
 * {@link #handle(PlotStarsEvent)} mutates the scene graph (via
 * {@code plotManager.drawAstrographicData}) and must therefore be called
 * on the JavaFX Application Thread. The listener wraps the call in
 * {@code FxThread.runOnFxThread(...)}; tests that exercise just the pure
 * pieces ({@link #calculateCenterCoordinates}, {@link #calculateDisplayRadius})
 * have no such requirement.
 */
@Slf4j
@Component
public class PlotStarsCoordinator {

    private final TripsContext tripsContext;
    private final RoutingPanel routingPanel;
    private final SearchContextCoordinator searchContextCoordinator;
    private final ApplicationEventPublisher eventPublisher;

    public PlotStarsCoordinator(TripsContext tripsContext,
                                RoutingPanel routingPanel,
                                SearchContextCoordinator searchContextCoordinator,
                                ApplicationEventPublisher eventPublisher) {
        this.tripsContext = tripsContext;
        this.routingPanel = routingPanel;
        this.searchContextCoordinator = searchContextCoordinator;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Validate, orchestrate, and execute the plot operation for a
     * {@link PlotStarsEvent}. Must be invoked on the JavaFX Application
     * Thread because {@code plotManager.drawAstrographicData} mutates the
     * 3D scene graph.
     * <p>
     * {@code plotManager} is passed in explicitly rather than autowired
     * because it is constructed late (in
     * {@code MainSplitPaneManager.initialize(...)}, after the Spring
     * context is fully wired) and isn't a {@code @Component}.
     */
    public void handle(PlotStarsEvent event, PlotManager plotManager) {
        List<StarObject> starObjects = event.getStarObjects();
        DataSetDescriptor descriptor = event.getDataSetDescriptor();

        if (starObjects == null || starObjects.isEmpty()) {
            showErrorAlert("Plot Stars", "No stars to plot");
            return;
        }
        if (descriptor == null) {
            showErrorAlert("Plot Stars", "No dataset descriptor available");
            return;
        }

        ColorPalette colorPalette = tripsContext.getAppViewPreferences().getColorPalette();
        StarDisplayPreferences starDisplayPreferences =
                tripsContext.getAppViewPreferences().getStarDisplayPreferences();
        CivilizationDisplayPreferences civilizationDisplayPreferences =
                tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences();

        double[] centerCoordinates = calculateCenterCoordinates(starObjects);
        double displayRadius = calculateDisplayRadius(starObjects, centerCoordinates);

        searchContextCoordinator.setDescriptor(descriptor);

        plotManager.drawAstrographicData(
                descriptor,
                starObjects,
                displayRadius,
                centerCoordinates,
                colorPalette,
                starDisplayPreferences,
                civilizationDisplayPreferences
        );

        routingPanel.setContext(descriptor, plotManager.getRouteVisibility());

        String statusMsg = event.getDescription() != null
                ? String.format("Plotted %d stars (%s)", starObjects.size(), event.getDescription())
                : "Plotted %d stars".formatted(starObjects.size());
        eventPublisher.publishEvent(new StatusUpdateEvent(this, statusMsg));

        log.info("Plotted {} stars from PlotStarsEvent", starObjects.size());
    }

    /**
     * Geometric centroid of a list of stars in light-year Cartesian space.
     * Returns {0, 0, 0} for an empty list. Pure function — safe to call
     * off the FX thread, ideal for unit-testing.
     */
    public static double[] calculateCenterCoordinates(List<StarObject> starObjects) {
        if (starObjects.isEmpty()) {
            return new double[]{0, 0, 0};
        }
        double sumX = 0, sumY = 0, sumZ = 0;
        for (StarObject star : starObjects) {
            sumX += star.getX();
            sumY += star.getY();
            sumZ += star.getZ();
        }
        int count = starObjects.size();
        return new double[]{sumX / count, sumY / count, sumZ / count};
    }

    /**
     * Display-sphere radius (in light years) that encompasses every star
     * in {@code starObjects} relative to {@code center}, with a 20%
     * padding factor and a 10 ly minimum to avoid degenerate views.
     * Returns 20 ly for an empty list (an arbitrary "nothing here" default
     * preserved from the original inline implementation).
     */
    public static double calculateDisplayRadius(List<StarObject> starObjects, double[] center) {
        if (starObjects.isEmpty()) {
            return 20.0;
        }
        double maxDistance = 0;
        for (StarObject star : starObjects) {
            double dx = star.getX() - center[0];
            double dy = star.getY() - center[1];
            double dz = star.getZ() - center[2];
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            maxDistance = Math.max(maxDistance, distance);
        }
        return Math.max(maxDistance * 1.2, 10.0);
    }
}
