package com.teamgannon.trips.dialogs.preferences;

import com.teamgannon.trips.config.application.ApplicationPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.listener.PreferencesUpdaterListener;
import com.teamgannon.trips.support.AlertFactory;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;


@Slf4j
public class ViewPreferencesDialog extends Dialog<ApplicationPreferences> {

    private final ApplicationPreferences preferences;
    private final PreferencesUpdaterListener updater;
    private final TripsContext tripsContext;
    private ButtonType buttonTypeOk;

    private GraphPane graphPane;
    private LinksPane linksPane;
    private StarsPane starsPane;
    private RoutePane routePane;
    private CivilizationPane civilizationPane;
    private UserControlsPane userControlsPane;


    public ViewPreferencesDialog(PreferencesUpdaterListener updater,
                                 TripsContext tripsContext,
                                 ApplicationPreferences preferences) {
        this.updater = updater;
        this.tripsContext = tripsContext;

        this.preferences = preferences;
        this.setTitle("View Preferences");

        VBox vBox = new VBox();
        createTabPanes(vBox);
        createButtons(vBox);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void close(WindowEvent windowEvent) {
        setResult(new ApplicationPreferences());
    }

    private void createTabPanes(@NotNull VBox vBox) {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: white");

        Tab displayTab = new Tab("Graph");
        String style1 = "-fx-background-color: lightgreen";
        displayTab.setStyle(style1);
        graphPane = new GraphPane(updater, tripsContext);
        displayTab.setContent(graphPane);
        tabPane.getTabs().add(displayTab);

        Tab linksTab = new Tab("Links");
        String style2 = "-fx-background-color: limegreen";
        linksTab.setStyle(style2);
        linksPane = new LinksPane(preferences.getLinkDisplayPreferences(), style2);
        linksTab.setContent(linksPane);
        tabPane.getTabs().add(linksTab);

        Tab starsTab = new Tab("Stars");
        String style3 = "-fx-background-color: aquamarine";
        starsTab.setStyle(style3);
        starsPane = new StarsPane(tripsContext.getAppViewPreferences().getStarDisplayPreferences(), updater);
        starsTab.setContent(starsPane);
        tabPane.getTabs().add(starsTab);

        Tab routeTab = new Tab("Route");
        String style5 = "-fx-background-color: lightyellow";
        routeTab.setStyle(style5);
        routePane = new RoutePane(preferences.getRouteDisplayPreferences(), style5);
        routeTab.setContent(routePane);
        tabPane.getTabs().add(routeTab);

        Tab civilizationTab = new Tab("Civilization");
        String style6 = "-fx-background-color: limegreen";
        civilizationTab.setStyle(style6);
        civilizationPane = new CivilizationPane(tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences(), updater);
        civilizationTab.setContent(civilizationPane);
        tabPane.getTabs().add(civilizationTab);

        Tab userControlsTab = new Tab("User Controls");
        String style7 = "-fx-background-color: lightblue";
        userControlsTab.setStyle(style7);
        userControlsPane = new UserControlsPane(tripsContext.getAppViewPreferences().getUserControls(), updater);
        userControlsTab.setContent(userControlsPane);
        tabPane.getTabs().add(userControlsTab);
        vBox.getChildren().add(tabPane);
    }

    private void createButtons(@NotNull VBox vBox) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.BASELINE_RIGHT);

        Button resetButton = new Button("Master Reset");
        resetButton.setOnAction(this::masterReset);
        hBox.getChildren().add(resetButton);

        Button cancelButton = new Button("Close");
        cancelButton.setOnAction(this::close);
        hBox.getChildren().add(cancelButton);

        vBox.getChildren().add(hBox);
    }

    private void masterReset(ActionEvent actionEvent) {
        Optional<ButtonType> buttonTypeOptional = AlertFactory.showConfirmationAlert(
                "Application Preferences",
                "Reset confirmation",
                "Are you sure that you want to reset all to default?");
        if (buttonTypeOptional.isPresent()) {
            ButtonType buttonType = buttonTypeOptional.get();
            if (buttonType.equals(ButtonType.OK)) {
                log.warn("Resetting all defaults");
                graphPane.reset();
                linksPane.reset();
                starsPane.reset();
                routePane.reset();
                civilizationPane.reset();
                userControlsPane.reset();
            }
        }
    }

    private void close(ActionEvent actionEvent) {
        setResult(new ApplicationPreferences());
    }

}
