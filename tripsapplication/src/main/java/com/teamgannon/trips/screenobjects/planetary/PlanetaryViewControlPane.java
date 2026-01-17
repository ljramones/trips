package com.teamgannon.trips.screenobjects.planetary;

import com.teamgannon.trips.planetary.PlanetaryContext;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Controls for adjusting the planetary sky view.
 * Includes time of day, viewing direction, magnitude limit, and field of view.
 */
@Slf4j
@Component
public class PlanetaryViewControlPane extends VBox {

    private final Slider timeOfDaySlider;
    private final Label timeOfDayLabel;
    private final ComboBox<String> viewDirectionCombo;
    private final Slider magnitudeLimitSlider;
    private final Label magnitudeLimitLabel;
    private final CheckBox atmosphereCheckbox;
    private final CheckBox orientationGridCheckbox;
    private final CheckBox showLabelsCheckbox;
    private final Slider labelMagnitudeSlider;
    private final Label labelMagnitudeLabel;

    private Consumer<Double> onTimeChanged;
    private Consumer<String> onDirectionChanged;
    private Consumer<Double> onMagnitudeChanged;
    private Consumer<Boolean> onAtmosphereChanged;
    private Consumer<Boolean> onOrientationGridChanged;
    private Consumer<Boolean> onShowLabelsChanged;
    private Consumer<Double> onLabelMagnitudeChanged;
    private Consumer<Preset> onPresetSelected;

    public enum Preset {
        ZENITH,
        HIGH_SKY,
        HORIZON,
        NADIR,
        FOCUS_BRIGHTEST
    }

    public PlanetaryViewControlPane() {
        setPadding(new Insets(10));
        setSpacing(10);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;

        // Time of day slider
        grid.add(new Label("Time of Day:"), 0, row);
        timeOfDaySlider = new Slider(0, 24, 22);
        timeOfDaySlider.setShowTickLabels(true);
        timeOfDaySlider.setShowTickMarks(true);
        timeOfDaySlider.setMajorTickUnit(6);
        timeOfDaySlider.setMinorTickCount(2);
        timeOfDaySlider.setBlockIncrement(1);
        grid.add(timeOfDaySlider, 1, row);
        timeOfDayLabel = new Label("22:00");
        grid.add(timeOfDayLabel, 2, row++);

        timeOfDaySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double time = newVal.doubleValue();
            int hours = (int) time;
            int minutes = (int) ((time - hours) * 60);
            timeOfDayLabel.setText(String.format("%02d:%02d", hours, minutes));
            if (onTimeChanged != null) {
                onTimeChanged.accept(time);
            }
        });

        // View direction
        grid.add(new Label("Look Toward:"), 0, row);
        viewDirectionCombo = new ComboBox<>();
        viewDirectionCombo.getItems().addAll("North", "East", "South", "West", "Zenith");
        viewDirectionCombo.setValue("North");
        grid.add(viewDirectionCombo, 1, row++, 2, 1);

        viewDirectionCombo.setOnAction(e -> {
            if (onDirectionChanged != null) {
                onDirectionChanged.accept(viewDirectionCombo.getValue());
            }
        });

        // Camera presets
        grid.add(new Label("Presets:"), 0, row);
        FlowPane presetRow = new FlowPane(6, 6);
        presetRow.setAlignment(Pos.CENTER_LEFT);
        presetRow.prefWrapLengthProperty().bind(widthProperty().subtract(40));
        Button zenithButton = presetButton("Zenith", "Look straight up");
        Button highSkyButton = presetButton("High", "Upper sky view");
        Button horizonButton = presetButton("Horizon", "Horizon view");
        Button nadirButton = presetButton("Nadir", "Look straight down");
        Button focusButton = presetButton("Focus*", "Center on brightest visible star");
        zenithButton.setOnAction(e -> firePreset(Preset.ZENITH));
        highSkyButton.setOnAction(e -> firePreset(Preset.HIGH_SKY));
        horizonButton.setOnAction(e -> firePreset(Preset.HORIZON));
        nadirButton.setOnAction(e -> firePreset(Preset.NADIR));
        focusButton.setOnAction(e -> firePreset(Preset.FOCUS_BRIGHTEST));
        presetRow.getChildren().addAll(zenithButton, highSkyButton, horizonButton, nadirButton, focusButton);
        grid.add(presetRow, 1, row++, 2, 1);

        // Magnitude limit slider
        grid.add(new Label("Magnitude Limit:"), 0, row);
        magnitudeLimitSlider = new Slider(1, 10, 6);
        magnitudeLimitSlider.setShowTickLabels(true);
        magnitudeLimitSlider.setShowTickMarks(true);
        magnitudeLimitSlider.setMajorTickUnit(2);
        magnitudeLimitSlider.setBlockIncrement(0.5);
        grid.add(magnitudeLimitSlider, 1, row);
        magnitudeLimitLabel = new Label("6.0");
        grid.add(magnitudeLimitLabel, 2, row++);

        magnitudeLimitSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double mag = newVal.doubleValue();
            magnitudeLimitLabel.setText(String.format("%.1f", mag));
            if (onMagnitudeChanged != null) {
                onMagnitudeChanged.accept(mag);
            }
        });

        // Atmosphere effects checkbox
        atmosphereCheckbox = new CheckBox("Show Atmosphere Effects");
        atmosphereCheckbox.setSelected(true);
        grid.add(atmosphereCheckbox, 0, row++, 3, 1);

        atmosphereCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (onAtmosphereChanged != null) {
                onAtmosphereChanged.accept(newVal);
            }
        });

        // Orientation grid checkbox
        orientationGridCheckbox = new CheckBox("Show Orientation Grid");
        orientationGridCheckbox.setSelected(false);
        grid.add(orientationGridCheckbox, 0, row++, 3, 1);

        orientationGridCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (onOrientationGridChanged != null) {
                onOrientationGridChanged.accept(newVal);
            }
        });

        // Star labels checkbox
        showLabelsCheckbox = new CheckBox("Show Star Labels");
        showLabelsCheckbox.setSelected(true);
        grid.add(showLabelsCheckbox, 0, row++, 3, 1);

        // Label magnitude limit slider (create before adding checkbox listener)
        grid.add(new Label("Label Mag Limit:"), 0, row);
        labelMagnitudeSlider = new Slider(0, 6, 3);
        labelMagnitudeSlider.setShowTickLabels(true);
        labelMagnitudeSlider.setShowTickMarks(true);
        labelMagnitudeSlider.setMajorTickUnit(1);
        labelMagnitudeSlider.setBlockIncrement(0.5);
        labelMagnitudeSlider.setSnapToTicks(false);
        grid.add(labelMagnitudeSlider, 1, row);
        labelMagnitudeLabel = new Label("3.0");
        grid.add(labelMagnitudeLabel, 2, row++);

        labelMagnitudeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double mag = newVal.doubleValue();
            labelMagnitudeLabel.setText(String.format("%.1f", mag));
            if (onLabelMagnitudeChanged != null) {
                onLabelMagnitudeChanged.accept(mag);
            }
        });

        // Now add the checkbox listener (after slider is initialized)
        showLabelsCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            labelMagnitudeSlider.setDisable(!newVal);
            if (onShowLabelsChanged != null) {
                onShowLabelsChanged.accept(newVal);
            }
        });

        // Separator
        grid.add(new Separator(), 0, row++, 3, 1);

        // Help text
        Label helpLabel = new Label("Drag mouse to rotate view\nScroll to zoom in/out");
        helpLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 10;");
        grid.add(helpLabel, 0, row, 3, 1);

        getChildren().add(grid);
    }

    /**
     * Set the context to initialize control values.
     */
    public void setContext(PlanetaryContext context) {
        if (context == null) {
            reset();
            return;
        }

        double localTime = context.getLocalTime();
        if (localTime == 0.0) {
            localTime = 22.0;
        }
        timeOfDaySlider.setValue(localTime);
        magnitudeLimitSlider.setValue(context.getMagnitudeLimit());
        atmosphereCheckbox.setSelected(context.isShowAtmosphereEffects());
        orientationGridCheckbox.setSelected(context.isShowOrientationGrid());

        // Set viewing direction based on azimuth
        double azimuth = context.getViewingAzimuth();
        String direction = azimuthToDirection(azimuth);
        viewDirectionCombo.setValue(direction);
    }

    /**
     * Reset controls to default values.
     */
    public void reset() {
        timeOfDaySlider.setValue(22);
        viewDirectionCombo.setValue("North");
        magnitudeLimitSlider.setValue(6);
        atmosphereCheckbox.setSelected(true);
        orientationGridCheckbox.setSelected(false);
        showLabelsCheckbox.setSelected(true);
        labelMagnitudeSlider.setValue(3);
        labelMagnitudeSlider.setDisable(false);
    }

    /**
     * Get the current time of day value.
     */
    public double getTimeOfDay() {
        return timeOfDaySlider.getValue();
    }

    /**
     * Get the current viewing direction.
     */
    public String getViewDirection() {
        return viewDirectionCombo.getValue();
    }

    /**
     * Get the current magnitude limit.
     */
    public double getMagnitudeLimit() {
        return magnitudeLimitSlider.getValue();
    }

    /**
     * Check if atmosphere effects are enabled.
     */
    public boolean isAtmosphereEnabled() {
        return atmosphereCheckbox.isSelected();
    }

    /**
     * Check if star labels are enabled.
     */
    public boolean isShowLabelsEnabled() {
        return showLabelsCheckbox.isSelected();
    }

    /**
     * Get the current label magnitude limit.
     */
    public double getLabelMagnitudeLimit() {
        return labelMagnitudeSlider.getValue();
    }

    /**
     * Set callbacks for control changes.
     */
    public void setOnTimeChanged(Consumer<Double> callback) {
        this.onTimeChanged = callback;
    }

    public void setOnDirectionChanged(Consumer<String> callback) {
        this.onDirectionChanged = callback;
    }

    public void setOnMagnitudeChanged(Consumer<Double> callback) {
        this.onMagnitudeChanged = callback;
    }

    public void setOnAtmosphereChanged(Consumer<Boolean> callback) {
        this.onAtmosphereChanged = callback;
    }

    public void setOnOrientationGridChanged(Consumer<Boolean> callback) {
        this.onOrientationGridChanged = callback;
    }

    public void setOnShowLabelsChanged(Consumer<Boolean> callback) {
        this.onShowLabelsChanged = callback;
    }

    public void setOnLabelMagnitudeChanged(Consumer<Double> callback) {
        this.onLabelMagnitudeChanged = callback;
    }

    public void setOnPresetSelected(Consumer<Preset> callback) {
        this.onPresetSelected = callback;
    }

    private void firePreset(Preset preset) {
        if (onPresetSelected != null) {
            onPresetSelected.accept(preset);
        }
    }

    private Button presetButton(String text, String tooltip) {
        Button button = new Button(text);
        button.setTooltip(new Tooltip(tooltip));
        button.setMinWidth(72);
        button.setPrefWidth(72);
        button.setFocusTraversable(false);
        return button;
    }

    /**
     * Convert azimuth to direction name.
     */
    private String azimuthToDirection(double azimuth) {
        azimuth = ((azimuth % 360) + 360) % 360;

        if (azimuth < 45 || azimuth >= 315) return "North";
        if (azimuth < 135) return "East";
        if (azimuth < 225) return "South";
        return "West";
    }

    /**
     * Convert direction name to azimuth.
     */
    public static double directionToAzimuth(String direction) {
        return switch (direction) {
            case "North" -> 0;
            case "East" -> 90;
            case "South" -> 180;
            case "West" -> 270;
            case "Zenith" -> 0;  // Zenith doesn't change azimuth
            default -> 0;
        };
    }

    /**
     * Get altitude for direction (Zenith is special case).
     */
    public static double directionToAltitude(String direction) {
        if ("Zenith".equals(direction)) {
            return 90;  // Straight up
        }
        return 65;  // Default altitude
    }
}
