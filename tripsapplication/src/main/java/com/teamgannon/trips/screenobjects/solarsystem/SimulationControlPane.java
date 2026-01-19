package com.teamgannon.trips.screenobjects.solarsystem;

import com.teamgannon.trips.controller.MainPane;
import com.teamgannon.trips.events.SolarSystemAnimationEvent;
import com.teamgannon.trips.events.SolarSystemCameraEvent;
import com.teamgannon.trips.events.SolarSystemDisplayToggleEvent;
import com.teamgannon.trips.events.SolarSystemScaleEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Controls for solar system visualization:
 * - Animation/time controls (for future orbital animation)
 * - Scale mode (linear/logarithmic)
 * - Display toggles (orbits, labels, habitable zone)
 */
@Slf4j
@Component
public class SimulationControlPane extends VBox {

    private final ApplicationEventPublisher eventPublisher;

    // Animation controls
    private final Button playPauseButton = new Button("\u25B6");  // Play symbol
    private final Button resetButton = new Button("\u23EE");  // Reset symbol
    private final Slider timeScaleSlider = new Slider(0.1, 10, 1);
    private final Label timeScaleLabel = new Label("1.0x");

    // Scale controls
    @Getter
    private final ComboBox<String> scaleModeCombo = new ComboBox<>();
    private final Slider zoomSlider = new Slider(0.5, 5, 1);
    private final Label zoomLabel = new Label("1.0x");

    // Display toggles
    @Getter
    private final CheckBox showOrbitsCheckbox = new CheckBox("Show Orbits");
    @Getter
    private final CheckBox showLabelsCheckbox = new CheckBox("Show Labels");
    @Getter
    private final CheckBox showHabitableZoneCheckbox = new CheckBox("Show Habitable Zone");
    @Getter
    private final CheckBox showGridCheckbox = new CheckBox("Show Scale Grid");
    @Getter
    private final CheckBox showRelativeSizesCheckbox = new CheckBox("True Relative Planet Sizes");

    private final Button topDownPresetButton = new Button("Top");
    private final Button edgeOnPresetButton = new Button("Edge");
    private final Button obliquePresetButton = new Button("45\u00B0");
    private final Button focusPresetButton = new Button("Focus");

    private boolean isPlaying = false;

    public SimulationControlPane(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;

        setPrefWidth(MainPane.SIDE_PANEL_SIZE);
        setPadding(new Insets(10));
        setSpacing(15);

        // Animation section
        VBox animationSection = createAnimationSection();

        // Scale section
        VBox scaleSection = createScaleSection();

        // Display section
        VBox displaySection = createDisplaySection();

        getChildren().addAll(
                animationSection,
                new Separator(),
                scaleSection,
                new Separator(),
                displaySection
        );

        // Set up listeners
        setupListeners();
    }

    private VBox createAnimationSection() {
        Label header = new Label("Animation");
        header.setStyle("-fx-font-weight: bold;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        playPauseButton.setPrefWidth(40);
        playPauseButton.setTooltip(new Tooltip("Play/Pause orbital animation"));

        resetButton.setPrefWidth(40);
        resetButton.setTooltip(new Tooltip("Reset to starting positions"));

        buttonBox.getChildren().addAll(playPauseButton, resetButton);

        // Time scale slider
        Label timeLabel = new Label("Time Scale:");
        timeScaleSlider.setShowTickLabels(true);
        timeScaleSlider.setShowTickMarks(true);
        timeScaleSlider.setMajorTickUnit(2);
        timeScaleSlider.setPrefWidth(180);

        HBox timeBox = new HBox(10);
        timeBox.setAlignment(Pos.CENTER_LEFT);
        timeBox.getChildren().addAll(timeLabel, timeScaleSlider, timeScaleLabel);

        VBox section = new VBox(8);
        section.getChildren().addAll(header, buttonBox, timeBox);
        return section;
    }

    private VBox createScaleSection() {
        Label header = new Label("Scale & Zoom");
        header.setStyle("-fx-font-weight: bold;");

        // Scale mode
        Label modeLabel = new Label("Scale Mode:");
        scaleModeCombo.getItems().addAll("Auto", "Linear", "Logarithmic");
        scaleModeCombo.setValue("Auto");
        scaleModeCombo.setTooltip(new Tooltip("Logarithmic scale shows distant planets more clearly"));

        HBox modeBox = new HBox(10);
        modeBox.setAlignment(Pos.CENTER_LEFT);
        modeBox.getChildren().addAll(modeLabel, scaleModeCombo);

        // Zoom slider
        Label zoomTitleLabel = new Label("Zoom:");
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setMajorTickUnit(1);
        zoomSlider.setPrefWidth(180);

        HBox zoomBox = new HBox(10);
        zoomBox.setAlignment(Pos.CENTER_LEFT);
        zoomBox.getChildren().addAll(zoomTitleLabel, zoomSlider, zoomLabel);

        VBox section = new VBox(8);
        section.getChildren().addAll(header, modeBox, zoomBox);
        return section;
    }

    private VBox createDisplaySection() {
        Label header = new Label("Display Options");
        header.setStyle("-fx-font-weight: bold;");

        // Set default states
        showOrbitsCheckbox.setSelected(true);
        showLabelsCheckbox.setSelected(true);
        showHabitableZoneCheckbox.setSelected(true);
        showGridCheckbox.setSelected(true);
        showRelativeSizesCheckbox.setSelected(false);  // Default to clamped sizes

        VBox checkboxes = new VBox(5);
        checkboxes.setPadding(new Insets(0, 0, 0, 10));
        checkboxes.getChildren().addAll(
                showOrbitsCheckbox,
                showLabelsCheckbox,
                showHabitableZoneCheckbox,
                showGridCheckbox,
                showRelativeSizesCheckbox
        );

        VBox section = new VBox(8);
        HBox presetRow = new HBox(6, topDownPresetButton, edgeOnPresetButton, obliquePresetButton, focusPresetButton);
        presetRow.setPadding(new Insets(4, 0, 0, 0));
        section.getChildren().addAll(header, checkboxes, new Separator(), presetRow);
        return section;
    }

    private void setupListeners() {
        // Play/Pause button
        playPauseButton.setOnAction(e -> {
            isPlaying = !isPlaying;
            playPauseButton.setText(isPlaying ? "\u23F8" : "\u25B6");  // Pause or Play symbol
            log.info("Animation {}", isPlaying ? "started" : "paused");
            eventPublisher.publishEvent(new SolarSystemAnimationEvent(
                    this, SolarSystemAnimationEvent.AnimationAction.TOGGLE_PLAY_PAUSE));
        });

        // Reset button
        resetButton.setOnAction(e -> {
            isPlaying = false;
            playPauseButton.setText("\u25B6");
            log.info("Animation reset");
            eventPublisher.publishEvent(new SolarSystemAnimationEvent(
                    this, SolarSystemAnimationEvent.AnimationAction.RESET));
        });

        // Time scale slider - convert 0.1-10 range to speed multiplier
        // 1.0 on slider = 1 day/sec (86400x), scaled logarithmically
        timeScaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            timeScaleLabel.setText(String.format("%.1fx", newVal.doubleValue()));
            // Convert slider value to speed multiplier:
            // slider 0.1 -> 1 hour/sec (3600)
            // slider 1.0 -> 1 day/sec (86400)
            // slider 10.0 -> 1 year/sec (31536000)
            double speedMultiplier = 86400.0 * newVal.doubleValue();
            log.debug("Time scale changed to: {} (speed: {}x)", newVal, speedMultiplier);
            eventPublisher.publishEvent(new SolarSystemAnimationEvent(this, speedMultiplier));
        });

        // Zoom slider
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoomLabel.setText(String.format("%.1fx", newVal.doubleValue()));
            log.info("Zoom changed to: {}", newVal);
            eventPublisher.publishEvent(new SolarSystemScaleEvent(this, newVal.doubleValue()));
        });

        // Scale mode
        scaleModeCombo.setOnAction(e -> {
            String mode = scaleModeCombo.getValue();
            SolarSystemScaleEvent.ScaleMode scaleMode = switch (mode) {
                case "Logarithmic" -> SolarSystemScaleEvent.ScaleMode.LOGARITHMIC;
                case "Linear" -> SolarSystemScaleEvent.ScaleMode.LINEAR;
                default -> SolarSystemScaleEvent.ScaleMode.AUTO;
            };
            log.info("Scale mode changed to: {}", scaleMode);
            eventPublisher.publishEvent(new SolarSystemScaleEvent(this, scaleMode));
        });

        // Display toggles
        showOrbitsCheckbox.setOnAction(e -> {
            boolean enabled = showOrbitsCheckbox.isSelected();
            log.info("Show orbits: {}", enabled);
            eventPublisher.publishEvent(new SolarSystemDisplayToggleEvent(
                    this, SolarSystemDisplayToggleEvent.ToggleType.ORBITS, enabled));
        });

        showLabelsCheckbox.setOnAction(e -> {
            boolean enabled = showLabelsCheckbox.isSelected();
            log.info("Show labels: {}", enabled);
            eventPublisher.publishEvent(new SolarSystemDisplayToggleEvent(
                    this, SolarSystemDisplayToggleEvent.ToggleType.LABELS, enabled));
        });

        showHabitableZoneCheckbox.setOnAction(e -> {
            boolean enabled = showHabitableZoneCheckbox.isSelected();
            log.info("Show habitable zone: {}", enabled);
            eventPublisher.publishEvent(new SolarSystemDisplayToggleEvent(
                    this, SolarSystemDisplayToggleEvent.ToggleType.HABITABLE_ZONE, enabled));
        });

        showGridCheckbox.setOnAction(e -> {
            boolean enabled = showGridCheckbox.isSelected();
            log.info("Show grid: {}", enabled);
            eventPublisher.publishEvent(new SolarSystemDisplayToggleEvent(
                    this, SolarSystemDisplayToggleEvent.ToggleType.SCALE_GRID, enabled));
        });

        showRelativeSizesCheckbox.setOnAction(e -> {
            boolean enabled = showRelativeSizesCheckbox.isSelected();
            log.info("Show relative planet sizes: {}", enabled);
            eventPublisher.publishEvent(new SolarSystemDisplayToggleEvent(
                    this, SolarSystemDisplayToggleEvent.ToggleType.RELATIVE_PLANET_SIZES, enabled));
        });

        topDownPresetButton.setOnAction(e -> eventPublisher.publishEvent(
                new SolarSystemCameraEvent(this, SolarSystemCameraEvent.CameraAction.TOP_DOWN)));
        edgeOnPresetButton.setOnAction(e -> eventPublisher.publishEvent(
                new SolarSystemCameraEvent(this, SolarSystemCameraEvent.CameraAction.EDGE_ON)));
        obliquePresetButton.setOnAction(e -> eventPublisher.publishEvent(
                new SolarSystemCameraEvent(this, SolarSystemCameraEvent.CameraAction.OBLIQUE)));
        focusPresetButton.setOnAction(e -> eventPublisher.publishEvent(
                new SolarSystemCameraEvent(this, SolarSystemCameraEvent.CameraAction.FOCUS_SELECTED)));
    }

    /**
     * Reset controls to default state.
     */
    public void reset() {
        isPlaying = false;
        playPauseButton.setText("\u25B6");
        timeScaleSlider.setValue(1.0);
        zoomSlider.setValue(1.0);
        scaleModeCombo.setValue("Auto");
        showOrbitsCheckbox.setSelected(true);
        showLabelsCheckbox.setSelected(true);
        showHabitableZoneCheckbox.setSelected(true);
        showGridCheckbox.setSelected(true);
        showRelativeSizesCheckbox.setSelected(false);
    }

    /**
     * Get the current zoom level.
     */
    public double getZoomLevel() {
        return zoomSlider.getValue();
    }

    /**
     * Get the current time scale.
     */
    public double getTimeScale() {
        return timeScaleSlider.getValue();
    }

    /**
     * Check if animation is playing.
     */
    public boolean isPlaying() {
        return isPlaying;
    }
}
