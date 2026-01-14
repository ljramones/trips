package com.teamgannon.trips.screenobjects.planetary;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.planetary.PlanetaryContext;
import com.teamgannon.trips.planetary.rendering.PlanetarySkyRenderer;
import javafx.scene.control.Accordion;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Main container for the planetary side pane content.
 * Contains an accordion with 4 titled panes:
 * 1. Viewing Location - planet info, viewing position
 * 2. Sky Overview - visible star count, host star position
 * 3. Brightest Stars - top 20 from this location
 * 4. View Controls - time, direction, magnitude limit
 */
@Slf4j
@Component
public class PlanetarySidePane extends VBox {

    @Getter
    private final Accordion planetaryAccordion = new Accordion();

    // Content panes
    private final ViewingLocationPane viewingLocationPane;
    private final SkyOverviewPane skyOverviewPane;
    private final BrightestStarsPane brightestStarsPane;
    private final PlanetaryViewControlPane viewControlPane;

    // TitledPanes
    private final TitledPane locationTitledPane;
    private final TitledPane overviewTitledPane;
    private final TitledPane starsTitledPane;
    private final TitledPane controlsTitledPane;

    private PlanetaryContext currentContext;

    public PlanetarySidePane(ViewingLocationPane viewingLocationPane,
                             SkyOverviewPane skyOverviewPane,
                             BrightestStarsPane brightestStarsPane,
                             PlanetaryViewControlPane viewControlPane) {
        this.viewingLocationPane = viewingLocationPane;
        this.skyOverviewPane = skyOverviewPane;
        this.brightestStarsPane = brightestStarsPane;
        this.viewControlPane = viewControlPane;

        // Match the interstellar side pane width exactly
        setMinWidth(MainPane.SIDE_PANEL_SIZE);
        setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        setMaxWidth(Double.MAX_VALUE);
        setFillWidth(true);

        // Ensure accordion also fills the width
        planetaryAccordion.setMinWidth(MainPane.SIDE_PANEL_SIZE);
        planetaryAccordion.setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        planetaryAccordion.setMaxWidth(Double.MAX_VALUE);

        // Create TitledPanes
        locationTitledPane = new TitledPane("Viewing Location", wrapInScrollPane(viewingLocationPane));
        locationTitledPane.setMinHeight(150);
        locationTitledPane.setMaxHeight(250);

        overviewTitledPane = new TitledPane("Sky Overview", wrapInScrollPane(skyOverviewPane));
        overviewTitledPane.setMinHeight(150);
        overviewTitledPane.setMaxHeight(250);

        starsTitledPane = new TitledPane("Brightest Stars", wrapInScrollPane(brightestStarsPane));
        starsTitledPane.setMinHeight(200);
        starsTitledPane.setMaxHeight(400);

        controlsTitledPane = new TitledPane("View Controls", wrapInScrollPane(viewControlPane));
        controlsTitledPane.setMinHeight(200);
        controlsTitledPane.setMaxHeight(350);

        // Add to accordion
        planetaryAccordion.getPanes().addAll(
                locationTitledPane,
                overviewTitledPane,
                starsTitledPane,
                controlsTitledPane
        );

        // Default to location expanded
        planetaryAccordion.setExpandedPane(locationTitledPane);

        getChildren().add(planetaryAccordion);

        log.info("PlanetarySidePane initialized with 4 titled panes");
    }

    private ScrollPane wrapInScrollPane(VBox content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }

    /**
     * Set the planetary context to display.
     */
    public void setContext(PlanetaryContext context) {
        this.currentContext = context;

        viewingLocationPane.setContext(context);
        viewControlPane.setContext(context);

        // Overview and brightest stars need the star list - will be updated separately
        skyOverviewPane.clear();
        brightestStarsPane.clear();

        // Expand location pane when context changes
        planetaryAccordion.setExpandedPane(locationTitledPane);

        log.info("PlanetarySidePane updated for planet: {}",
                context != null ? context.getPlanetName() : "null");
    }

    /**
     * Update the brightest stars list.
     */
    public void updateBrightestStars(List<PlanetarySkyRenderer.BrightStarEntry> stars) {
        skyOverviewPane.setContext(currentContext, stars);
        brightestStarsPane.setStars(stars);
    }

    /**
     * Clear all displayed information.
     */
    public void clear() {
        this.currentContext = null;
        viewingLocationPane.clear();
        skyOverviewPane.clear();
        brightestStarsPane.clear();
        viewControlPane.reset();
    }

    /**
     * Get the current planetary context.
     */
    public PlanetaryContext getCurrentContext() {
        return currentContext;
    }

    /**
     * Expand the brightest stars pane.
     */
    public void expandStarsList() {
        planetaryAccordion.setExpandedPane(starsTitledPane);
    }

    /**
     * Expand the controls pane.
     */
    public void expandControls() {
        planetaryAccordion.setExpandedPane(controlsTitledPane);
    }

    /**
     * Get the view control pane for callback registration.
     */
    public PlanetaryViewControlPane getViewControlPane() {
        return viewControlPane;
    }

    /**
     * Get the brightest stars pane for callback registration.
     */
    public BrightestStarsPane getBrightestStarsPane() {
        return brightestStarsPane;
    }
}
