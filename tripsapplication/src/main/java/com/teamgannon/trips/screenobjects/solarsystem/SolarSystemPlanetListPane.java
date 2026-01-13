package com.teamgannon.trips.screenobjects.solarsystem;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.events.SolarSystemObjectSelectedEvent;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;

/**
 * Displays a list of planets in the current solar system.
 * Includes context menu for planet operations.
 */
@Slf4j
@Component
public class SolarSystemPlanetListPane extends VBox {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

    private final ListView<PlanetDescription> planetListView = new ListView<>();
    private final ApplicationEventPublisher eventPublisher;
    private SolarSystemDescription currentSystem;

    public SolarSystemPlanetListPane(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;

        setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        setPrefHeight(300);
        setPadding(new Insets(5));

        planetListView.setPrefWidth(MainPane.SIDE_PANEL_SIZE - 10);
        planetListView.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(planetListView, Priority.ALWAYS);

        planetListView.setCellFactory(lv -> new PlanetListCell());
        planetListView.setPlaceholder(new Label("No planets in system"));

        // Selection listener - publish event when planet selected
        planetListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                eventPublisher.publishEvent(new SolarSystemObjectSelectedEvent(
                        this, newVal, SolarSystemObjectSelectedEvent.SelectionType.PLANET));
            }
        });

        getChildren().add(planetListView);
    }

    /**
     * Update the list with planets from the given solar system.
     */
    public void setSystem(SolarSystemDescription system) {
        this.currentSystem = system;
        planetListView.getItems().clear();

        if (system != null && system.getPlanetDescriptionList() != null) {
            List<PlanetDescription> planets = system.getPlanetDescriptionList();
            // Sort by semi-major axis (distance from star)
            planets.sort(Comparator.comparingDouble(PlanetDescription::getSemiMajorAxis));
            planetListView.getItems().addAll(planets);
            log.debug("SolarSystemPlanetListPane updated with {} planets/moons", planets.size());
        }
    }

    /**
     * Clear the planet list.
     */
    public void clear() {
        currentSystem = null;
        planetListView.getItems().clear();
        planetListView.getSelectionModel().clearSelection();
    }

    /**
     * Get the currently selected planet.
     */
    public PlanetDescription getSelectedPlanet() {
        return planetListView.getSelectionModel().getSelectedItem();
    }

    /**
     * Select a planet by name.
     */
    public void selectPlanet(String planetName) {
        for (PlanetDescription planet : planetListView.getItems()) {
            if (planet.getName() != null && planet.getName().equals(planetName)) {
                planetListView.getSelectionModel().select(planet);
                planetListView.scrollTo(planet);
                break;
            }
        }
    }

    /**
     * Custom list cell for displaying planets with context menu.
     */
    private class PlanetListCell extends ListCell<PlanetDescription> {

        private final ContextMenu contextMenu = new ContextMenu();

        public PlanetListCell() {
            MenuItem propertiesItem = new MenuItem("Properties...");
            propertiesItem.setOnAction(e -> {
                PlanetDescription planet = getItem();
                if (planet != null) {
                    log.info("Properties requested for planet: {}", planet.getName());
                    // TODO: Open properties dialog
                }
            });

            MenuItem editOrbitItem = new MenuItem("Edit Orbit...");
            editOrbitItem.setOnAction(e -> {
                PlanetDescription planet = getItem();
                if (planet != null) {
                    log.info("Edit orbit requested for planet: {}", planet.getName());
                    // TODO: Open orbit editor
                }
            });

            MenuItem highlightItem = new MenuItem("Highlight in View");
            highlightItem.setOnAction(e -> {
                PlanetDescription planet = getItem();
                if (planet != null) {
                    log.info("Highlight requested for planet: {}", planet.getName());
                    // TODO: Highlight planet in 3D view
                }
            });

            SeparatorMenuItem separator = new SeparatorMenuItem();

            MenuItem deleteItem = new MenuItem("Delete Planet");
            deleteItem.setOnAction(e -> {
                PlanetDescription planet = getItem();
                if (planet != null) {
                    log.info("Delete requested for planet: {}", planet.getName());
                    // TODO: Confirm and delete planet
                }
            });

            contextMenu.getItems().addAll(propertiesItem, editOrbitItem, highlightItem, separator, deleteItem);
        }

        @Override
        protected void updateItem(PlanetDescription planet, boolean empty) {
            super.updateItem(planet, empty);

            if (empty || planet == null) {
                setText(null);
                setGraphic(null);
                setContextMenu(null);
            } else {
                StringBuilder sb = new StringBuilder();

                // Icon prefix based on type
                if (planet.isMoon()) {
                    sb.append("\u25CB ");  // Small circle for moon
                } else {
                    sb.append("\u25CF ");  // Filled circle for planet
                }

                // Name
                sb.append(planet.getName() != null ? planet.getName() : "Unknown");

                // Orbit distance
                sb.append(" (");
                sb.append(DECIMAL_FORMAT.format(planet.getSemiMajorAxis()));
                sb.append(" AU)");

                setText(sb.toString());
                setContextMenu(contextMenu);
            }
        }
    }
}
