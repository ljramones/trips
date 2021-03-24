package com.teamgannon.trips.dialogs.preferences;

import com.teamgannon.trips.config.application.UserControls;
import com.teamgannon.trips.listener.PreferencesUpdaterListener;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

public class UserControlsPane extends Pane {

    private final static String USER_PANE_TITLE = "Mouse Drag Rotation Direction";
    private final static String USER_PANE_TITLE_MODIFIED = "Mouse Drag Rotation Direction - *modified*";
    private final PreferencesUpdaterListener updater;

    private final UserControls userControls;

    private final @NotNull TitledPane controlsPane;

    private final CheckBox userSenseCheckBox = new CheckBox("Invert from default");


    public UserControlsPane(UserControls userControls, PreferencesUpdaterListener updater) {
        this.userControls = userControls;
        this.updater = updater;

        VBox vBox = new VBox();

        Pane pane1 = createControlsPane();
        controlsPane = new TitledPane(USER_PANE_TITLE, pane1);
        vBox.getChildren().add(controlsPane);

        this.getChildren().add(vBox);
    }

    private Pane createControlsPane() {
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        userSenseCheckBox.setSelected(userControls.isControlSense());

        gridPane.add(userSenseCheckBox, 0, 0);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Button resetBtn = new Button("Reset");
        resetBtn.setOnAction(this::resetColorsClicked);
        hBox.getChildren().add(resetBtn);

        Button addBtn = new Button("Change");
        addBtn.setOnAction(this::changeColorsClicked);
        hBox.getChildren().add(addBtn);

        gridPane.add(hBox, 0, 2, 2, 1);

        return gridPane;
    }

    private void resetColorsClicked(ActionEvent actionEvent) {
        userControls.reset();
        userSenseCheckBox.setSelected(userControls.isControlSense());
    }

    private void changeColorsClicked(ActionEvent actionEvent) {
        userControls.setControlSense(userSenseCheckBox.isSelected());
        updater.changeUserControlsPreferences(userControls);
    }


}
