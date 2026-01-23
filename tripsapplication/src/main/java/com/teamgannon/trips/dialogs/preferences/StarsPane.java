package com.teamgannon.trips.dialogs.preferences;

import com.teamgannon.trips.config.application.model.StarDescriptionPreference;
import com.teamgannon.trips.config.application.model.StarDisplayPreferences;
import com.teamgannon.trips.events.StarDisplayPreferencesChangeEvent;
import com.teamgannon.trips.stellarmodelling.StellarType;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

import java.util.EnumMap;
import java.util.Map;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

/**
 * Pane for editing star display preferences (colors and sizes by stellar class).
 */
@Slf4j
public class StarsPane extends Pane {

    private static final String STAR_PANE_TITLE = "Change Star Colors";
    private static final String STAR_PANE_TITLE_MODIFIED = "Change Star Colors - *modified*";

    // Stellar types to display (in order)
    private static final StellarType[] STELLAR_TYPES = {
            StellarType.O, StellarType.B, StellarType.A, StellarType.F, StellarType.G,
            StellarType.K, StellarType.M, StellarType.L, StellarType.T, StellarType.Y
    };

    private final ApplicationEventPublisher eventPublisher;
    private final Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
    private final @NotNull StarDisplayPreferences starDisplayPreferences;

    // UI components stored by stellar type
    private final Map<StellarType, TextField> radiusFields = new EnumMap<>(StellarType.class);
    private final Map<StellarType, TextField> colorFields = new EnumMap<>(StellarType.class);
    private final Map<StellarType, ColorPicker> colorPickers = new EnumMap<>(StellarType.class);

    private final TextField numberOfLabelsTextField = new TextField("30");
    private final @NotNull TitledPane starPane;

    public StarsPane(@NotNull StarDisplayPreferences starDisplayPreferences,
                     ApplicationEventPublisher eventPublisher) {
        this.starDisplayPreferences = starDisplayPreferences;
        this.eventPublisher = eventPublisher;

        VBox vBox = new VBox();
        Pane pane1 = createStarPane();
        starPane = new TitledPane(STAR_PANE_TITLE, pane1);
        vBox.getChildren().add(starPane);
        this.getChildren().add(vBox);
    }

    private @NotNull Pane createStarPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        // Header row
        gridPane.add(new Label("Stellar Type"), 0, 0);
        gridPane.add(new Label("Radius (Sol Units)"), 1, 0);
        gridPane.add(new Label("Color Value"), 2, 0);
        gridPane.add(new Label("Color Swatch"), 3, 0);

        // Create rows for each stellar type
        int row = 1;
        for (StellarType type : STELLAR_TYPES) {
            createStellarClassRow(gridPane, row++, type);
        }

        // Number of labels field
        Label numberOfStarLabelsLabel = new Label("# of Labels");
        numberOfStarLabelsLabel.setFont(font);
        numberOfLabelsTextField.setText(Integer.toString(starDisplayPreferences.getNumberOfVisibleLabels()));
        gridPane.add(numberOfStarLabelsLabel, 0, row);
        gridPane.add(numberOfLabelsTextField, 1, row);
        row++;

        // Buttons
        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER);

        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(this::resetColorsClicked);
        hBox.getChildren().add(resetBtn);

        Button changeBtn = new Button("Change");
        changeBtn.setOnAction(this::changeColorsClicked);
        hBox.getChildren().add(changeBtn);

        gridPane.add(hBox, 0, row + 1, 4, 1);

        return gridPane;
    }

    /**
     * Create a row for a single stellar class with radius field, color field, and color picker.
     */
    private void createStellarClassRow(GridPane gridPane, int row, StellarType type) {
        StarDescriptionPreference star = starDisplayPreferences.get(type);

        // Label
        Label label = new Label(type.getValue() + " Class:");
        label.setFont(font);
        gridPane.add(label, 0, row);

        // Radius text field
        TextField radiusField = new TextField();
        radiusField.setText(Float.toString(star.getSize()));
        gridPane.add(radiusField, 1, row);
        radiusFields.put(type, radiusField);

        // Color text field
        TextField colorField = new TextField();
        colorField.setText(star.getColor().toString());
        gridPane.add(colorField, 2, row);
        colorFields.put(type, colorField);

        // Color picker
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(star.getColor());
        gridPane.add(colorPicker, 3, row);
        colorPickers.put(type, colorPicker);

        // Wire up color picker to update text field
        colorPicker.setOnAction(e -> {
            Color c = colorPicker.getValue();
            colorField.setText(c.toString());
            starPane.setText(STAR_PANE_TITLE_MODIFIED);
        });

        // Wire up text field to update color picker on Enter
        colorField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                handleColorFieldChanged(type);
            }
        });
    }

    /**
     * Handle color text field change - validate and update color picker.
     */
    private void handleColorFieldChanged(StellarType type) {
        TextField colorField = colorFields.get(type);
        ColorPicker colorPicker = colorPickers.get(type);

        try {
            Color color = Color.valueOf(colorField.getText());
            colorPicker.setValue(color);
            starPane.setText(STAR_PANE_TITLE_MODIFIED);
        } catch (Exception e) {
            log.error("{} is an invalid color for {} class", colorField.getText(), type.getValue());
            showErrorAlert("Change " + type.getValue() + " Class Color",
                    colorField.getText() + " is an invalid color");
        }
    }

    /**
     * Apply changes to star display preferences.
     */
    private void changeColorsClicked(ActionEvent actionEvent) {
        Map<StellarType, StarDescriptionPreference> starMap = starDisplayPreferences.getStarMap();

        // Update all stellar types from UI
        for (StellarType type : STELLAR_TYPES) {
            StarDescriptionPreference star = starMap.get(type);
            if (star != null) {
                try {
                    star.setSize(Float.parseFloat(radiusFields.get(type).getText()));
                    star.setColor(colorPickers.get(type).getValue());
                } catch (NumberFormatException e) {
                    showErrorAlert("Star Preferences",
                            "Invalid radius value for " + type.getValue() + " class");
                    return;
                }
            }
        }

        // Update number of visible labels
        try {
            starDisplayPreferences.setNumberOfVisibleLabels(
                    Integer.parseInt(numberOfLabelsTextField.getText()));
        } catch (NumberFormatException nfe) {
            showErrorAlert("Star Preferences", "Number of labels must be an integer!");
            return;
        }

        starPane.setText(STAR_PANE_TITLE);
        eventPublisher.publishEvent(new StarDisplayPreferencesChangeEvent(this, starDisplayPreferences));
        log.info("Star display preferences changed and event published");
    }

    /**
     * Reset all star preferences to defaults.
     */
    private void resetColorsClicked(ActionEvent actionEvent) {
        starDisplayPreferences.reset();

        // Update UI from reset preferences
        for (StellarType type : STELLAR_TYPES) {
            StarDescriptionPreference star = starDisplayPreferences.getStarMap().get(type);
            if (star != null) {
                radiusFields.get(type).setText(Float.toString(star.getSize()));
                colorFields.get(type).setText(star.getColor().toString());
                colorPickers.get(type).setValue(star.getColor());
            }
        }

        numberOfLabelsTextField.setText(
                Integer.toString(starDisplayPreferences.getNumberOfVisibleLabels()));
        starPane.setText(STAR_PANE_TITLE);
    }

    /**
     * Public reset method called from ViewPreferencesDialog.
     */
    public void reset() {
        resetColorsClicked(new ActionEvent());
        eventPublisher.publishEvent(new StarDisplayPreferencesChangeEvent(this, starDisplayPreferences));
    }
}
