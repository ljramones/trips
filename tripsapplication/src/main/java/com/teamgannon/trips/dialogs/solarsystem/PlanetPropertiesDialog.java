package com.teamgannon.trips.dialogs.solarsystem;

import com.teamgannon.trips.jpa.model.ExoPlanet;
import com.teamgannon.trips.planetarymodelling.PlanetDescription;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Dialog for viewing and editing exoplanet properties.
 * Changes to orbital parameters trigger a redraw of the solar system visualization.
 */
@Slf4j
public class PlanetPropertiesDialog extends Dialog<PlanetEditResult> {

    private final ExoPlanet planet;
    private final List<PlanetDescription> siblingPlanets;

    // Original values for change detection
    private final Double origSemiMajorAxis;
    private final Double origEccentricity;
    private final Double origInclination;
    private final Double origOmega;
    private final Double origOrbitalPeriod;

    // Identity fields
    private TextField nameField;
    private TextField statusField;
    private TextField discoveredField;
    private TextField starNameField;

    // Orbital parameter fields
    private TextField semiMajorAxisField;
    private TextField eccentricityField;
    private TextField inclinationField;
    private TextField omegaField;  // Argument of periapsis
    private TextField orbitalPeriodField;

    // Physical property fields
    private TextField radiusField;
    private TextField massField;
    private TextField tempField;

    // Validation label
    private Label validationLabel;

    public PlanetPropertiesDialog(@NotNull ExoPlanet planet, List<PlanetDescription> siblingPlanets) {
        this.planet = planet;
        this.siblingPlanets = siblingPlanets;

        // Store original orbital values for change detection
        this.origSemiMajorAxis = planet.getSemiMajorAxis();
        this.origEccentricity = planet.getEccentricity();
        this.origInclination = planet.getInclination();
        this.origOmega = planet.getOmega();
        this.origOrbitalPeriod = planet.getOrbitalPeriod();

        setTitle("Planet Properties: " + planet.getName());

        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(15));

        // Identity section
        mainBox.getChildren().add(createIdentitySection());

        // Orbital parameters section
        mainBox.getChildren().add(createOrbitalSection());

        // Physical properties section
        mainBox.getChildren().add(createPhysicalSection());

        // Validation message
        validationLabel = new Label("");
        validationLabel.setTextFill(Color.ORANGE);
        mainBox.getChildren().add(validationLabel);

        // Buttons
        mainBox.getChildren().add(createButtonBar());

        this.getDialogPane().setContent(mainBox);
        this.getDialogPane().setPrefWidth(450);

        DialogUtils.bindCloseHandler(this, this::handleClose);

        // Populate fields with current values
        populateFields();
    }

    private TitledPane createIdentitySection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Name:"), 0, 0);
        nameField = new TextField();
        nameField.setPrefWidth(250);
        grid.add(nameField, 1, 0);

        grid.add(createBoldLabel("Status:"), 0, 1);
        statusField = new TextField();
        grid.add(statusField, 1, 1);

        grid.add(createBoldLabel("Discovered:"), 0, 2);
        discoveredField = new TextField();
        grid.add(discoveredField, 1, 2);

        grid.add(createBoldLabel("Host Star:"), 0, 3);
        starNameField = new TextField();
        starNameField.setEditable(false);
        starNameField.setStyle("-fx-background-color: #f0f0f0;");
        grid.add(starNameField, 1, 3);

        TitledPane pane = new TitledPane("Identity", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createOrbitalSection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Semi-major Axis (AU):"), 0, 0);
        semiMajorAxisField = new TextField();
        semiMajorAxisField.textProperty().addListener((obs, old, newVal) -> validateOrbits());
        grid.add(semiMajorAxisField, 1, 0);

        grid.add(createBoldLabel("Eccentricity:"), 0, 1);
        eccentricityField = new TextField();
        grid.add(eccentricityField, 1, 1);

        grid.add(createBoldLabel("Inclination (deg):"), 0, 2);
        inclinationField = new TextField();
        grid.add(inclinationField, 1, 2);

        grid.add(createBoldLabel("Arg. of Periapsis (deg):"), 0, 3);
        omegaField = new TextField();
        grid.add(omegaField, 1, 3);

        grid.add(createBoldLabel("Orbital Period (days):"), 0, 4);
        orbitalPeriodField = new TextField();
        grid.add(orbitalPeriodField, 1, 4);

        TitledPane pane = new TitledPane("Orbital Parameters", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private TitledPane createPhysicalSection() {
        GridPane grid = createGridPane();

        grid.add(createBoldLabel("Radius (Earth radii):"), 0, 0);
        radiusField = new TextField();
        grid.add(radiusField, 1, 0);

        grid.add(createBoldLabel("Mass (Earth masses):"), 0, 1);
        massField = new TextField();
        grid.add(massField, 1, 1);

        grid.add(createBoldLabel("Eq. Temperature (K):"), 0, 2);
        tempField = new TextField();
        grid.add(tempField, 1, 2);

        TitledPane pane = new TitledPane("Physical Properties", grid);
        pane.setCollapsible(false);
        return pane;
    }

    private HBox createButtonBar() {
        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(10, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(this::handleCancel);

        Button okBtn = new Button("OK");
        okBtn.setDefaultButton(true);
        okBtn.setOnAction(this::handleOk);

        hbox.getChildren().addAll(cancelBtn, okBtn);
        return hbox;
    }

    private GridPane createGridPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        return grid;
    }

    private Label createBoldLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        return label;
    }

    private void populateFields() {
        // Identity
        nameField.setText(planet.getName() != null ? planet.getName() : "");
        statusField.setText(planet.getPlanetStatus() != null ? planet.getPlanetStatus() : "");
        discoveredField.setText(planet.getDiscovered() != null ? planet.getDiscovered().toString() : "");
        starNameField.setText(planet.getStarName() != null ? planet.getStarName() : "");

        // Orbital
        semiMajorAxisField.setText(formatDouble(planet.getSemiMajorAxis()));
        eccentricityField.setText(formatDouble(planet.getEccentricity()));
        inclinationField.setText(formatDouble(planet.getInclination()));
        omegaField.setText(formatDouble(planet.getOmega()));
        orbitalPeriodField.setText(formatDouble(planet.getOrbitalPeriod()));

        // Physical
        radiusField.setText(formatDouble(planet.getRadius()));
        massField.setText(formatDouble(planet.getMass()));
        tempField.setText(formatDouble(planet.getTempCalculated()));
    }

    private String formatDouble(Double value) {
        if (value == null) return "";
        return String.format("%.6g", value);
    }

    private Double parseDouble(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(String text) {
        if (text == null || text.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void validateOrbits() {
        Double newSMA = parseDouble(semiMajorAxisField.getText());
        if (newSMA == null || newSMA <= 0) {
            validationLabel.setText("");
            return;
        }

        // Check if any sibling planet has a similar orbit
        for (PlanetDescription sibling : siblingPlanets) {
            if (sibling.getName().equals(planet.getName())) {
                continue;  // Skip self
            }
            double sibSMA = sibling.getSemiMajorAxis();
            if (sibSMA > 0 && Math.abs(newSMA - sibSMA) / sibSMA < 0.05) {
                validationLabel.setText("Warning: Orbit overlaps with " + sibling.getName() +
                        " (SMA: " + String.format("%.4f", sibSMA) + " AU)");
                return;
            }
        }
        validationLabel.setText("");
    }

    private void handleOk(ActionEvent event) {
        // Update planet with new values
        planet.setName(nameField.getText().trim());
        planet.setPlanetStatus(statusField.getText().trim());
        planet.setDiscovered(parseInteger(discoveredField.getText()));

        // Orbital parameters
        planet.setSemiMajorAxis(parseDouble(semiMajorAxisField.getText()));
        planet.setEccentricity(parseDouble(eccentricityField.getText()));
        planet.setInclination(parseDouble(inclinationField.getText()));
        planet.setOmega(parseDouble(omegaField.getText()));
        planet.setOrbitalPeriod(parseDouble(orbitalPeriodField.getText()));

        // Physical properties
        planet.setRadius(parseDouble(radiusField.getText()));
        planet.setMass(parseDouble(massField.getText()));
        planet.setTempCalculated(parseDouble(tempField.getText()));

        // Determine if anything changed
        boolean orbitalChanged = hasOrbitalChanged();
        boolean anyChanged = orbitalChanged || hasNonOrbitalChanged();

        log.info("Planet edit: changed={}, orbitalChanged={}", anyChanged, orbitalChanged);

        setResult(PlanetEditResult.changed(planet, orbitalChanged));
    }

    private boolean hasOrbitalChanged() {
        return !nullSafeEquals(origSemiMajorAxis, planet.getSemiMajorAxis()) ||
               !nullSafeEquals(origEccentricity, planet.getEccentricity()) ||
               !nullSafeEquals(origInclination, planet.getInclination()) ||
               !nullSafeEquals(origOmega, planet.getOmega());
    }

    private boolean hasNonOrbitalChanged() {
        // Simplified check - could be more thorough
        return true;  // Assume something changed if OK was clicked
    }

    private boolean nullSafeEquals(Double a, Double b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return Math.abs(a - b) < 0.0000001;
    }

    private void handleCancel(ActionEvent event) {
        setResult(PlanetEditResult.unchanged(planet));
    }

    private void handleClose(WindowEvent event) {
        setResult(PlanetEditResult.unchanged(planet));
    }
}
