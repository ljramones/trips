package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.dialogs.solarsystem.PlanetEditResult;
import com.teamgannon.trips.dialogs.solarsystem.PlanetPropertiesDialog;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Factory for creating context menus for solar system visualization elements.
 */
@Slf4j
@Component
public class SolarSystemContextMenuFactory {

    /**
     * Create a context menu for a planet sphere.
     *
     * @param planet           the planet description
     * @param exoPlanet        the ExoPlanet entity for editing
     * @param siblingPlanets   other planets in the system (for orbit validation)
     * @param onEditComplete   callback when editing is complete
     * @param onDeletePlanet   callback when planet should be deleted
     * @return the context menu
     */
    public ContextMenu createPlanetContextMenu(
            PlanetDescription planet,
            ExoPlanet exoPlanet,
            List<PlanetDescription> siblingPlanets,
            Consumer<PlanetEditResult> onEditComplete,
            Consumer<ExoPlanet> onDeletePlanet) {

        ContextMenu menu = new ContextMenu();

        // Title item (disabled, just for display)
        MenuItem titleItem = new MenuItem(planet.getName());
        titleItem.setStyle("-fx-text-fill: darkblue; -fx-font-size: 14; -fx-font-weight: bold;");
        titleItem.setDisable(true);
        menu.getItems().add(titleItem);
        menu.getItems().add(new SeparatorMenuItem());

        // Properties menu item
        MenuItem propertiesItem = new MenuItem("Properties...");
        propertiesItem.setOnAction(e -> {
            if (exoPlanet != null) {
                showPlanetPropertiesDialog(exoPlanet, siblingPlanets, onEditComplete);
            } else {
                showReadOnlyPlanetInfo(planet);
            }
        });
        menu.getItems().add(propertiesItem);

        // Edit orbit (shortcut to properties focused on orbital tab)
        MenuItem editOrbitItem = new MenuItem("Edit Orbit...");
        editOrbitItem.setOnAction(e -> {
            if (exoPlanet != null) {
                showPlanetPropertiesDialog(exoPlanet, siblingPlanets, onEditComplete);
            }
        });
        editOrbitItem.setDisable(exoPlanet == null);
        menu.getItems().add(editOrbitItem);

        menu.getItems().add(new SeparatorMenuItem());

        // Delete planet
        MenuItem deleteItem = new MenuItem("Delete Planet...");
        deleteItem.setOnAction(e -> {
            if (exoPlanet != null && onDeletePlanet != null) {
                confirmAndDeletePlanet(exoPlanet, onDeletePlanet);
            }
        });
        deleteItem.setDisable(exoPlanet == null);
        menu.getItems().add(deleteItem);

        return menu;
    }

    /**
     * Create a context menu for the central star.
     *
     * @param star              the star display record
     * @param onJumpToInterstellar callback to return to interstellar view
     * @return the context menu
     */
    public ContextMenu createStarContextMenu(
            StarDisplayRecord star,
            Runnable onJumpToInterstellar) {

        ContextMenu menu = new ContextMenu();

        // Title item
        MenuItem titleItem = new MenuItem(star.getStarName());
        titleItem.setStyle("-fx-text-fill: darkblue; -fx-font-size: 14; -fx-font-weight: bold;");
        titleItem.setDisable(true);
        menu.getItems().add(titleItem);
        menu.getItems().add(new SeparatorMenuItem());

        // Properties (show basic star info)
        MenuItem propertiesItem = new MenuItem("Star Properties...");
        propertiesItem.setOnAction(e -> showStarInfo(star));
        menu.getItems().add(propertiesItem);

        menu.getItems().add(new SeparatorMenuItem());

        // Jump to interstellar view
        MenuItem jumpItem = new MenuItem("Return to Interstellar View");
        jumpItem.setOnAction(e -> {
            if (onJumpToInterstellar != null) {
                onJumpToInterstellar.run();
            }
        });
        menu.getItems().add(jumpItem);

        return menu;
    }

    /**
     * Create a context menu for an orbit path (delegates to planet menu).
     */
    public ContextMenu createOrbitContextMenu(
            PlanetDescription planet,
            ExoPlanet exoPlanet,
            List<PlanetDescription> siblingPlanets,
            Consumer<PlanetEditResult> onEditComplete,
            Consumer<ExoPlanet> onDeletePlanet) {

        // Orbit context menu is same as planet menu
        return createPlanetContextMenu(planet, exoPlanet, siblingPlanets, onEditComplete, onDeletePlanet);
    }

    private void showPlanetPropertiesDialog(
            ExoPlanet exoPlanet,
            List<PlanetDescription> siblingPlanets,
            Consumer<PlanetEditResult> onEditComplete) {

        PlanetPropertiesDialog dialog = new PlanetPropertiesDialog(exoPlanet, siblingPlanets);
        Optional<PlanetEditResult> result = dialog.showAndWait();

        result.ifPresent(editResult -> {
            if (editResult.isChanged() && onEditComplete != null) {
                onEditComplete.accept(editResult);
            }
        });
    }

    private void showReadOnlyPlanetInfo(PlanetDescription planet) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Planet Information");
        alert.setHeaderText(planet.getName());

        StringBuilder sb = new StringBuilder();
        sb.append("Semi-major Axis: ").append(String.format("%.4f", planet.getSemiMajorAxis())).append(" AU\n");
        sb.append("Orbital Period: ").append(String.format("%.1f", planet.getOrbitalPeriod())).append(" days\n");
        sb.append("Eccentricity: ").append(String.format("%.4f", planet.getEccentricity())).append("\n");
        sb.append("Radius: ").append(String.format("%.2f", planet.getRadius())).append(" Earth radii\n");
        sb.append("Mass: ").append(String.format("%.2f", planet.getMass())).append(" Earth masses\n");

        if (planet.getEquilibriumTemperature() > 0) {
            sb.append("Eq. Temperature: ").append(String.format("%.0f", planet.getEquilibriumTemperature())).append(" K\n");
        }

        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    private void showStarInfo(StarDisplayRecord star) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Star Information");
        alert.setHeaderText(star.getStarName());

        StringBuilder sb = new StringBuilder();
        sb.append("Spectral Class: ").append(star.getSpectralClass()).append("\n");
        sb.append("Distance: ").append(String.format("%.2f", star.getDistance())).append(" ly\n");

        if (star.getRadius() > 0) {
            sb.append("Radius: ").append(String.format("%.2f", star.getRadius())).append(" solar radii\n");
        }
        if (star.getLuminosity() > 0) {
            sb.append("Luminosity: ").append(String.format("%.4f", star.getLuminosity())).append(" solar\n");
        }

        alert.setContentText(sb.toString());
        alert.showAndWait();
    }

    private void confirmAndDeletePlanet(ExoPlanet planet, Consumer<ExoPlanet> onDeletePlanet) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Planet");
        confirm.setHeaderText("Delete " + planet.getName() + "?");
        confirm.setContentText("This action cannot be undone. The planet will be permanently removed from the database.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            log.info("User confirmed deletion of planet: {}", planet.getName());
            onDeletePlanet.accept(planet);
        }
    }
}
