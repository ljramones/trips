package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.dialogs.solarsystem.AddPlanetDialog;
import com.teamgannon.trips.dialogs.solarsystem.PlanetEditResult;
import com.teamgannon.trips.dialogs.solarsystem.PlanetPropertiesDialog;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.planetarymodelling.SolarSystemDescription;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
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

        return createPlanetContextMenu(planet, exoPlanet, siblingPlanets, onEditComplete, onDeletePlanet, null);
    }

    /**
     * Create a context menu for a planet sphere with land on planet option.
     *
     * @param planet           the planet description
     * @param exoPlanet        the ExoPlanet entity for editing
     * @param siblingPlanets   other planets in the system (for orbit validation)
     * @param onEditComplete   callback when editing is complete
     * @param onDeletePlanet   callback when planet should be deleted
     * @param onLandOnPlanet   callback when user wants to land on planet (view sky from surface)
     * @return the context menu
     */
    public ContextMenu createPlanetContextMenu(
            PlanetDescription planet,
            ExoPlanet exoPlanet,
            List<PlanetDescription> siblingPlanets,
            Consumer<PlanetEditResult> onEditComplete,
            Consumer<ExoPlanet> onDeletePlanet,
            Consumer<ExoPlanet> onLandOnPlanet) {

        ContextMenu menu = new ContextMenu();

        // Title item (disabled, just for display)
        MenuItem titleItem = new MenuItem(planet.getName());
        titleItem.setStyle("-fx-text-fill: darkblue; -fx-font-size: 14; -fx-font-weight: bold;");
        titleItem.setDisable(true);
        menu.getItems().add(titleItem);
        menu.getItems().add(new SeparatorMenuItem());

        // Land on Planet (view sky from surface) - first for prominence
        MenuItem landOnPlanetItem = new MenuItem("Land on Planet");
        landOnPlanetItem.setOnAction(e -> {
            if (exoPlanet != null && onLandOnPlanet != null) {
                log.info("User selected 'Land on Planet' for: {}", exoPlanet.getName());
                onLandOnPlanet.accept(exoPlanet);
            }
        });
        landOnPlanetItem.setDisable(exoPlanet == null || onLandOnPlanet == null);
        menu.getItems().add(landOnPlanetItem);

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

        // Add Moon (only for planets, not moons)
        MenuItem addMoonItem = new MenuItem("Add Moon...");
        addMoonItem.setOnAction(e -> {
            if (exoPlanet != null) {
                showAddMoonDialog(exoPlanet, siblingPlanets, onEditComplete);
            }
        });
        // Disable if this is already a moon or if no exoPlanet
        addMoonItem.setDisable(exoPlanet == null || Boolean.TRUE.equals(exoPlanet.getIsMoon()));
        menu.getItems().add(addMoonItem);

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

        // Look up parent planet name if this is a moon
        String parentPlanetName = null;
        if (Boolean.TRUE.equals(exoPlanet.getIsMoon()) && exoPlanet.getParentPlanetId() != null) {
            parentPlanetName = findParentPlanetName(exoPlanet.getParentPlanetId(), siblingPlanets);
        }

        PlanetPropertiesDialog dialog = new PlanetPropertiesDialog(exoPlanet, siblingPlanets, parentPlanetName);
        Optional<PlanetEditResult> result = dialog.showAndWait();

        result.ifPresent(editResult -> {
            if (editResult.isChanged() && onEditComplete != null) {
                onEditComplete.accept(editResult);
            }
        });
    }

    /**
     * Find the parent planet name from the siblings list by ID.
     */
    private String findParentPlanetName(String parentPlanetId, List<PlanetDescription> siblings) {
        if (parentPlanetId == null || siblings == null) {
            return null;
        }
        for (PlanetDescription sibling : siblings) {
            if (parentPlanetId.equals(sibling.getId())) {
                return sibling.getName();
            }
        }
        return null;
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

    /**
     * Create a context menu for empty space in the solar system view.
     *
     * @param currentSystem the current solar system
     * @param onAddPlanet   callback when a new planet is added
     * @return the context menu
     */
    public ContextMenu createEmptySpaceContextMenu(
            SolarSystemDescription currentSystem,
            Consumer<ExoPlanet> onAddPlanet) {

        ContextMenu menu = new ContextMenu();

        String systemName = currentSystem.getStarDisplayRecord() != null
                ? currentSystem.getStarDisplayRecord().getStarName()
                : "System";

        // Title
        MenuItem titleItem = new MenuItem(systemName);
        titleItem.setStyle("-fx-text-fill: darkblue; -fx-font-size: 14; -fx-font-weight: bold;");
        titleItem.setDisable(true);
        menu.getItems().add(titleItem);
        menu.getItems().add(new SeparatorMenuItem());

        // Add Planet
        MenuItem addPlanetItem = new MenuItem("Add Planet...");
        addPlanetItem.setOnAction(e -> showAddPlanetDialog(currentSystem, onAddPlanet));
        menu.getItems().add(addPlanetItem);

        return menu;
    }

    /**
     * Show dialog to add a new planet to the system.
     */
    private void showAddPlanetDialog(SolarSystemDescription currentSystem, Consumer<ExoPlanet> onAddPlanet) {
        AddPlanetDialog dialog = new AddPlanetDialog(currentSystem, false, null);
        Optional<ExoPlanet> result = dialog.showAndWait();

        result.ifPresent(newPlanet -> {
            log.info("User created new planet: {}", newPlanet.getName());
            if (onAddPlanet != null) {
                onAddPlanet.accept(newPlanet);
            }
        });
    }

    /**
     * Show dialog to add a moon to a planet.
     */
    private void showAddMoonDialog(ExoPlanet parentPlanet,
                                    List<PlanetDescription> siblingPlanets,
                                    Consumer<PlanetEditResult> onEditComplete) {
        // Create a minimal system description for the dialog
        SolarSystemDescription tempDesc = new SolarSystemDescription();
        tempDesc.setSolarSystemId(parentPlanet.getSolarSystemId());
        tempDesc.setPlanetDescriptionList(siblingPlanets);

        AddPlanetDialog dialog = new AddPlanetDialog(tempDesc, true, parentPlanet);
        Optional<ExoPlanet> result = dialog.showAndWait();

        result.ifPresent(newMoon -> {
            log.info("User created new moon: {} for planet {}", newMoon.getName(), parentPlanet.getName());
            // Signal that we need to refresh by creating a dummy edit result
            if (onEditComplete != null) {
                // Create a result that signals orbital change to trigger refresh
                PlanetEditResult editResult = PlanetEditResult.changed(parentPlanet, true);
                onEditComplete.accept(editResult);
            }
        });
    }
}
