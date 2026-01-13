package com.teamgannon.trips.screenobjects.solarsystem;

import com.teamgannon.trips.controller.MainPane;
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

        // Note about animation being a future feature
        Label noteLabel = new Label("(Animation coming soon)");
        noteLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #888888; -fx-font-size: 10px;");

        VBox section = new VBox(8);
        section.getChildren().addAll(header, buttonBox, timeBox, noteLabel);
        return section;
    }

    private VBox createScaleSection() {
        Label header = new Label("Scale & Zoom");
        header.setStyle("-fx-font-weight: bold;");

        // Scale mode
        Label modeLabel = new Label("Scale Mode:");
        scaleModeCombo.getItems().addAll("Linear", "Logarithmic");
        scaleModeCombo.setValue("Linear");
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

        VBox checkboxes = new VBox(5);
        checkboxes.setPadding(new Insets(0, 0, 0, 10));
        checkboxes.getChildren().addAll(
                showOrbitsCheckbox,
                showLabelsCheckbox,
                showHabitableZoneCheckbox,
                showGridCheckbox
        );

        VBox section = new VBox(8);
        section.getChildren().addAll(header, checkboxes);
        return section;
    }

    private void setupListeners() {
        // Play/Pause button
        playPauseButton.setOnAction(e -> {
            isPlaying = !isPlaying;
            playPauseButton.setText(isPlaying ? "\u23F8" : "\u25B6");  // Pause or Play symbol
            log.info("Animation {}", isPlaying ? "started" : "paused");
            // TODO: Publish animation event
        });

        // Reset button
        resetButton.setOnAction(e -> {
            isPlaying = false;
            playPauseButton.setText("\u25B6");
            log.info("Animation reset");
            // TODO: Publish reset event
        });

        // Time scale slider
        timeScaleSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            timeScaleLabel.setText(String.format("%.1fx", newVal.doubleValue()));
            log.debug("Time scale changed to: {}", newVal);
            // TODO: Publish time scale change event
        });

        // Zoom slider
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoomLabel.setText(String.format("%.1fx", newVal.doubleValue()));
            log.debug("Zoom changed to: {}", newVal);
            // TODO: Publish zoom change event
        });

        // Scale mode
        scaleModeCombo.setOnAction(e -> {
            String mode = scaleModeCombo.getValue();
            log.info("Scale mode changed to: {}", mode);
            // TODO: Publish scale mode change event
        });

        // Display toggles
        showOrbitsCheckbox.setOnAction(e -> {
            log.info("Show orbits: {}", showOrbitsCheckbox.isSelected());
            // TODO: Publish display settings event
        });

        showLabelsCheckbox.setOnAction(e -> {
            log.info("Show labels: {}", showLabelsCheckbox.isSelected());
            // TODO: Publish display settings event
        });

        showHabitableZoneCheckbox.setOnAction(e -> {
            log.info("Show habitable zone: {}", showHabitableZoneCheckbox.isSelected());
            // TODO: Publish display settings event
        });

        showGridCheckbox.setOnAction(e -> {
            log.info("Show grid: {}", showGridCheckbox.isSelected());
            // TODO: Publish display settings event
        });
    }

    /**
     * Reset controls to default state.
     */
    public void reset() {
        isPlaying = false;
        playPauseButton.setText("\u25B6");
        timeScaleSlider.setValue(1.0);
        zoomSlider.setValue(1.0);
        scaleModeCombo.setValue("Linear");
        showOrbitsCheckbox.setSelected(true);
        showLabelsCheckbox.setSelected(true);
        showHabitableZoneCheckbox.setSelected(true);
        showGridCheckbox.setSelected(true);
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
