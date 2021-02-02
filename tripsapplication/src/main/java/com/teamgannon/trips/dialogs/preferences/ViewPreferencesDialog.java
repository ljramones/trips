package com.teamgannon.trips.dialogs.preferences;

import com.teamgannon.trips.config.application.ApplicationPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.listener.PreferencesUpdaterListener;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;


@Slf4j
public class ViewPreferencesDialog extends Dialog<ApplicationPreferences> {

    private final ApplicationPreferences preferences;
    private final PreferencesUpdaterListener updater;
    private final TripsContext tripsContext;
    private TabPane tabPane;
    private ButtonType buttonTypeOk;


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

        this.setResultConverter(button -> okButtonAction(preferences, button));

        this.getDialogPane().setContent(vBox);
    }

    private void createTabPanes(@NotNull VBox vBox) {
        tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: white");

        Tab displayTab = new Tab("Graph");
        String style1 = "-fx-background-color: lightgreen";
        displayTab.setStyle(style1);
        displayTab.setContent(new GraphPane(updater, tripsContext));
        tabPane.getTabs().add(displayTab);

        Tab linksTab = new Tab("Links");
        String style2 = "-fx-background-color: limegreen";
        linksTab.setStyle(style2);
        linksTab.setContent(new LinksPane(preferences.getLinkDisplayPreferences(), style2));
        tabPane.getTabs().add(linksTab);

        Tab starsTab = new Tab("Stars");
        String style3 = "-fx-background-color: aquamarine";
        starsTab.setStyle(style3);
        starsTab.setContent(new StarsPane(tripsContext.getAppViewPreferences().getStarDisplayPreferences(), updater));
        tabPane.getTabs().add(starsTab);

        Tab routeTab = new Tab("Route");
        String style5 = "-fx-background-color: lightyellow";
        routeTab.setStyle(style5);
        routeTab.setContent(new RoutePane(preferences.getRouteDisplayPreferences(), style5));
        tabPane.getTabs().add(routeTab);

        Tab civilizationTab = new Tab("Civilization");
        String style6 = "-fx-background-color: limegreen";
        civilizationTab.setStyle(style6);
        civilizationTab.setContent(
                new CivilizationPane(tripsContext.getAppViewPreferences().getCivilizationDisplayPreferences(), updater));
        tabPane.getTabs().add(civilizationTab);

        vBox.getChildren().add(tabPane);
    }

    private void createButtons(@NotNull VBox vBox) {
        HBox hBox = new HBox();

        ButtonType buttonTypeCancel = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        vBox.getChildren().add(hBox);
    }


    private ApplicationPreferences okButtonAction(ApplicationPreferences preferences, ButtonType button) {
        if (button == buttonTypeOk) {
            // note: read from the panels
            return new ApplicationPreferences();
        }
        return preferences;
    }


}
