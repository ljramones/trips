package com.teamgannon.trips.dialogs;

import com.teamgannon.trips.config.application.ApplicationPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.dialogs.preferences.GraphColorPane;
import com.teamgannon.trips.dialogs.support.*;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ApplicationPreferencesDialog extends Dialog<ViewPreferencesChange> {

    private final @NotNull GraphColorPane colorPane;

    private final @NotNull AppPrefsPane appPrefsPane;

    private final ApplicationPreferences applicationPreferences;
    // app preferences
    private final TextField routeSegmentLengthTextField = new TextField();
    public @NotNull Button changeButton = new Button("Change");
    public @NotNull Button resetColorsButton = new Button("Reset to defaults");

    /**
     * constructor
     *
     * @param tripsContext the trips context
     */
    public ApplicationPreferencesDialog(@NotNull TripsContext tripsContext) {
        this.applicationPreferences = tripsContext.getAppPreferences();

        this.setTitle("Change Application Preferences Dialog");
        this.setHeight(300);
        this.setWidth(400);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);
        this.getDialogPane().setContent(vBox);

        HBox prefsBox = new HBox();
        vBox.getChildren().add(prefsBox);

        colorPane = new GraphColorPane(tripsContext.getAppViewPreferences().getColorPallete());
        prefsBox.getChildren().add(colorPane);

        appPrefsPane = new AppPrefsPane(tripsContext.getAppPreferences());
        prefsBox.getChildren().add(appPrefsPane);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        changeButton.setOnAction(this::changeClicked);
        hBox.getChildren().add(changeButton);

        resetColorsButton.setOnAction(this::resetColorsClicked);
        hBox.getChildren().add(resetColorsButton);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(this::cancel);
        hBox.getChildren().add(cancelBtn);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

    }

    private void resetColorsClicked(ActionEvent actionEvent) {
        ColorChangeResult colorChangeResult = new ColorChangeResult(ChangeTypeEnum.RESET, null);
        ApplicationPreferencesChange applicationPreferencesChange = new ApplicationPreferencesChange(ChangeTypeEnum.RESET, null);
        ViewPreferencesChange viewPreferencesChange = new ViewPreferencesChange(colorChangeResult, applicationPreferencesChange);
        setResult(viewPreferencesChange);
    }

    private void close(WindowEvent windowEvent) {
        ColorChangeResult colorChangeResult = new ColorChangeResult(ChangeTypeEnum.CANCEL, null);
        ApplicationPreferencesChange applicationPreferencesChange = new ApplicationPreferencesChange(ChangeTypeEnum.CANCEL, null);
        ViewPreferencesChange viewPreferencesChange = new ViewPreferencesChange(colorChangeResult, applicationPreferencesChange);
        setResult(viewPreferencesChange);
    }

    private void cancel(ActionEvent actionEvent) {
        ColorChangeResult colorChangeResult = new ColorChangeResult(ChangeTypeEnum.CANCEL, null);
        ApplicationPreferencesChange applicationPreferencesChange = new ApplicationPreferencesChange(ChangeTypeEnum.CANCEL, null);
        ViewPreferencesChange viewPreferencesChange = new ViewPreferencesChange(colorChangeResult, applicationPreferencesChange);
        setResult(viewPreferencesChange);
    }

    private void changeClicked(ActionEvent actionEvent) {

        ViewPreferencesChange viewPreferencesChange = new ViewPreferencesChange();

        if (colorPane.isChanged()) {
            ColorPalette colorPalette = colorPane.getColorData();
            ColorChangeResult colorChangeResult = new ColorChangeResult(ChangeTypeEnum.CHANGE, colorPalette);
            viewPreferencesChange.setColorChangeResult(colorChangeResult);
        }

        if (appPrefsPane.isChanged()) {
            ApplicationPreferences applicationPreferences = appPrefsPane.getAppPrefs();
            ApplicationPreferencesChange applicationPreferencesChange = new ApplicationPreferencesChange(ChangeTypeEnum.CHANGE, applicationPreferences);
            viewPreferencesChange.setApplicationPreferencesChange(applicationPreferencesChange);
        }

        setResult(viewPreferencesChange);
    }

}
