package com.teamgannon.trips.screenobjects.solarsystem;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import javafx.scene.control.Accordion;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Main container for the solar system side pane content.
 * Contains an accordion with 4 titled panes:
 * 1. System Overview - star info, habitable zone, counts
 * 2. Planet List - list of planets with context menu
 * 3. Selected Object Properties - details of selected planet/star
 * 4. Simulation Controls - animation and display settings
 */
@Slf4j
@Component
public class SolarSystemSidePane extends VBox {

    @Getter
    private final Accordion solarSystemAccordion = new Accordion();

    // Content panes
    private final SystemOverviewPane systemOverviewPane;
    private final SolarSystemPlanetListPane planetListPane;
    private final SolarSystemObjectPropertiesPane objectPropertiesPane;
    private final SimulationControlPane simulationControlPane;

    // TitledPanes
    private final TitledPane overviewTitledPane;
    private final TitledPane planetListTitledPane;
    private final TitledPane propertiesTitledPane;
    private final TitledPane simulationTitledPane;

    private SolarSystemDescription currentSystem;

    public SolarSystemSidePane(SystemOverviewPane systemOverviewPane,
                                SolarSystemPlanetListPane planetListPane,
                                SolarSystemObjectPropertiesPane objectPropertiesPane,
                                SimulationControlPane simulationControlPane) {
        this.systemOverviewPane = systemOverviewPane;
        this.planetListPane = planetListPane;
        this.objectPropertiesPane = objectPropertiesPane;
        this.simulationControlPane = simulationControlPane;

        // Match the interstellar side pane width exactly
        setMinWidth(MainPane.SIDE_PANEL_SIZE);
        setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        setMaxWidth(Double.MAX_VALUE);
        setFillWidth(true);

        // Ensure accordion also fills the width
        solarSystemAccordion.setMinWidth(MainPane.SIDE_PANEL_SIZE);
        solarSystemAccordion.setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        solarSystemAccordion.setMaxWidth(Double.MAX_VALUE);

        // Create TitledPanes
        overviewTitledPane = new TitledPane("System Overview", wrapInScrollPane(systemOverviewPane));
        overviewTitledPane.setMinHeight(150);
        overviewTitledPane.setMaxHeight(300);

        planetListTitledPane = new TitledPane("Planets & Moons", wrapInScrollPane(planetListPane));
        planetListTitledPane.setMinHeight(200);
        planetListTitledPane.setMaxHeight(400);

        propertiesTitledPane = new TitledPane("Selected Object", wrapInScrollPane(objectPropertiesPane));
        propertiesTitledPane.setMinHeight(200);
        propertiesTitledPane.setMaxHeight(500);

        simulationTitledPane = new TitledPane("Display & Controls", wrapInScrollPane(simulationControlPane));
        simulationTitledPane.setMinHeight(150);
        simulationTitledPane.setMaxHeight(400);

        // Add to accordion
        solarSystemAccordion.getPanes().addAll(
                overviewTitledPane,
                planetListTitledPane,
                propertiesTitledPane,
                simulationTitledPane
        );

        // Default to overview expanded
        solarSystemAccordion.setExpandedPane(overviewTitledPane);

        getChildren().add(solarSystemAccordion);

        log.info("SolarSystemSidePane initialized with 4 titled panes");
    }

    private ScrollPane wrapInScrollPane(VBox content) {
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }

    /**
     * Set the solar system to display.
     * Updates all child panes with the system information.
     */
    public void setSystem(SolarSystemDescription system) {
        this.currentSystem = system;

        systemOverviewPane.setSystem(system);
        planetListPane.setSystem(system);
        objectPropertiesPane.clear();
        simulationControlPane.reset();

        // Expand overview pane when system changes
        solarSystemAccordion.setExpandedPane(overviewTitledPane);

        log.info("SolarSystemSidePane updated for system: {}",
                system != null && system.getStarDisplayRecord() != null
                        ? system.getStarDisplayRecord().getStarName()
                        : "null");
    }

    /**
     * Clear all displayed information.
     */
    public void clear() {
        this.currentSystem = null;
        systemOverviewPane.clear();
        planetListPane.clear();
        objectPropertiesPane.clear();
        simulationControlPane.reset();
    }

    /**
     * Get the current solar system being displayed.
     */
    public SolarSystemDescription getCurrentSystem() {
        return currentSystem;
    }

    /**
     * Expand the planet list pane.
     */
    public void expandPlanetList() {
        solarSystemAccordion.setExpandedPane(planetListTitledPane);
    }

    /**
     * Expand the properties pane.
     */
    public void expandProperties() {
        solarSystemAccordion.setExpandedPane(propertiesTitledPane);
    }

    /**
     * Expand the simulation controls pane.
     */
    public void expandControls() {
        solarSystemAccordion.setExpandedPane(simulationTitledPane);
    }
}
