package com.teamgannon.trips.dialog;

import com.teamgannon.trips.config.application.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class PreferencesDialog extends Dialog<ApplicationPreferences> {

    private Pane displayPane;

    private Pane starsPane;

    private Pane positionPane;

    private Pane routePane;

    private TabPane tabPane;

    private ButtonType buttonTypeOk;

    private Pane civilizationPane;
    private ApplicationPreferences preferences;


    public PreferencesDialog(ApplicationPreferences preferences) {

        this.preferences = preferences;
        this.setTitle("Application Preferences");

        VBox vBox = new VBox();
        createTabPanes(vBox);
        createButtons(vBox);

        this.setResultConverter(button -> okButtonAction(preferences, button));

        this.getDialogPane().setContent(vBox);
    }

    private ApplicationPreferences okButtonAction(ApplicationPreferences preferences, ButtonType button) {
        if (button == buttonTypeOk) {
            // note: read from the panels
            return new ApplicationPreferences();
        }
        return preferences;
    }

    private void createTabPanes(VBox vBox) {
        tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: white");

        Tab displayTab = new Tab("Grid");
        String style1 = "-fx-background-color: lightgreen";
        displayTab.setStyle(style1);
        displayTab.setContent(createGridPane(preferences.getDisplayPreferences(), style1));
        tabPane.getTabs().add(displayTab);

        Tab linksTab = new Tab("Links");
        String style2 = "-fx-background-color: limegreen";
        linksTab.setStyle(style2);
        linksTab.setContent(createLinksPane(preferences.getLinkDisplayPreferences(), style2));
        tabPane.getTabs().add(linksTab);

        Tab starsTab = new Tab("Stars");
        String style3 = "-fx-background-color: aquamarine";
        starsTab.setStyle(style3);
        starsTab.setContent(createStarsPane(preferences.getStarDisplayPreferences(), style3));
        tabPane.getTabs().add(starsTab);

        Tab positionTab = new Tab("Position");
        String style4 = "-fx-background-color: lavender";
        positionTab.setStyle(style4);
        positionTab.setContent(createPositionPane(preferences.getPositionDisplayPreferences(), style4));
        tabPane.getTabs().add(positionTab);

        Tab routeTab = new Tab("Route");
        String style5 = "-fx-background-color: lightyellow";
        routeTab.setStyle(style5);
        routeTab.setContent(createRoutePane(preferences.getRouteDisplayPreferences(), style5));
        tabPane.getTabs().add(routeTab);

        Tab civilizationTab = new Tab("Civilization");
        String style6 = "-fx-background-color: limegreen";
        civilizationTab.setStyle(style6);
        civilizationTab.setContent(createCivPane(preferences.getCivilizationDisplayPreferences(), style6));
        tabPane.getTabs().add(civilizationTab);

        vBox.getChildren().add(tabPane);
    }

    private void createButtons(VBox vBox) {
        HBox hBox = new HBox();

        buttonTypeOk = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().add(buttonTypeOk);

        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        vBox.getChildren().add(hBox);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////


    private Pane createGridPane(DisplayPreferences displayPreferences, String style) {

        // fill out display preferences
        GridPane gridPane = new GridPane();
        gridPane.setStyle(style);

        gridPane.add(new Label("       "), 1, 1);

        Pane pane1 = gridDescriptionPane();
        gridPane.add(pane1, 1, 2);

        gridPane.add(new Label("      "), 1, 3);

        return gridPane;
    }


    private Pane gridDescriptionPane() {

        GridPane pane = new GridPane();

        pane.add(new Separator(), 1, 1, 1, 4);

        CheckBox showGrid = new CheckBox("Display Grid");
        pane.add(showGrid, 2, 1);

        pane.add(new Label("Size (in Ly): "), 2, 2);
        TextField gridSize = new TextField("5");
        gridSize.setPrefWidth(20);
        pane.add(gridSize, 3, 2);

        pane.add(new Label("Grid color:"), 2, 3);
        ColorPicker gridColorPicker = new ColorPicker(Color.BLUE);
        pane.add(gridColorPicker, 3, 3);

        pane.add(new Label("Stem color:"), 2, 4);
        ColorPicker stemColorPicker = new ColorPicker(Color.BLUE);
        pane.add(stemColorPicker, 3, 4);

        return pane;
    }


    private Pane createLinksPane(LinkDisplayPreferences linksDefinition, String style) {
        GridPane gridPane = new GridPane();
        gridPane.setStyle(style);

        gridPane.add(new Label("       "), 1, 1);

        Pane pane1 = linksDescriptionPane(linksDefinition);
        gridPane.add(pane1, 1, 2);

        gridPane.add(new Label("      "), 1, 3);

        return gridPane;
    }

    private Pane linksDescriptionPane(LinkDisplayPreferences linksDefinition) {
        GridPane pane = new GridPane();

        pane.add(new Separator(), 1, 1, 1, 6);

        CheckBox showLinks = new CheckBox("Display Links");
        showLinks.setSelected(linksDefinition.isShowLinks());
        pane.add(showLinks, 2, 1, 2, 1);

        CheckBox showDistances = new CheckBox("Display Distances");
        showDistances.setSelected(linksDefinition.isShowDistances());
        pane.add(showDistances, 2, 2, 2, 1);

        createLinkLine(pane, 3, linksDefinition.getLinkDescriptorList().get(0));
        createLinkLine(pane, 4, linksDefinition.getLinkDescriptorList().get(1));
        createLinkLine(pane, 5, linksDefinition.getLinkDescriptorList().get(2));
        createLinkLine(pane, 6, linksDefinition.getLinkDescriptorList().get(3));

        return pane;
    }

    private void createLinkLine(GridPane pane, int row, LinkDescriptor linkDescriptor) {
        pane.add(
                new Label(
                        String.format("Link %d:", linkDescriptor.getLinkNumber())),
                2, row);
        TextField link1 = new TextField(Integer.toString(linkDescriptor.getLinkLength()));
        link1.setPrefWidth(30);
        pane.add(link1, 3, row);
        ColorPicker gridColorPicker = new ColorPicker(linkDescriptor.getColor());
        pane.add(gridColorPicker, 4, row);
    }

    private Node createCivPane(CivilizationDisplayPreferences civilizationDisplayPreferences, String style) {
        civilizationPane = new Pane();
        civilizationPane.setStyle(style);

        civilizationPane.getChildren().add(new Label("Civilization"));

        return civilizationPane;
    }

    private Node createRoutePane(RouteDisplayPreferences routeDisplayPreferences, String style) {
        routePane = new Pane();
        routePane.setStyle(style);

        routePane.getChildren().add(new Label("Route"));

        return routePane;
    }

    private Node createPositionPane(PositionDisplayPreferences positionDisplayPreferences, String style) {
        positionPane = new Pane();
        positionPane.setStyle(style);

        positionPane.getChildren().add(new Label("Position"));

        return positionPane;
    }

    private Node createStarsPane(StarDisplayPreferences starDisplayPreferences, String style) {
        GridPane gridPane = new GridPane();

        gridPane.setStyle(style);

        gridPane.add(new Label("       "), 1, 1);

        Pane pane1 = starsDefinitionPane(starDisplayPreferences);
        gridPane.add(pane1, 1, 2);

        gridPane.add(new Label("      "), 1, 3);
        return gridPane;

    }

    private Pane starsDefinitionPane(StarDisplayPreferences starDisplayPreferences) {
        GridPane pane = new GridPane();

        pane.add(new Separator(), 1, 1, 1, starDisplayPreferences.getStarMap().size() + 1);

        CheckBox showLinks = new CheckBox("Display Name");
        showLinks.setSelected(starDisplayPreferences.isShowStarName());
        pane.add(showLinks, 2, 1, 1, 1);

        pane.add(new Label("View Radius: "), 2, 2);
        TextField viewDistance = new TextField(Integer.toString(starDisplayPreferences.getViewRadius()));
        viewDistance.setPrefWidth(20);
        pane.add(viewDistance, 3, 2);

        int i = 0;
        for (StarDescriptionPreference star : starDisplayPreferences.getStarMap()) {
            createStarLine(pane, 3 + i, starDisplayPreferences.getStarMap().get(i++));
        }

        return pane;
    }

    private void createStarLine(GridPane pane, int row, StarDescriptionPreference starDescriptionPreference) {
        pane.add(
                new Label(
                        String.format("Stellar Class %s:", starDescriptionPreference.getStartClass().getValue())),
                2, row);

        ColorPicker gridColorPicker = new ColorPicker(starDescriptionPreference.getColor());
        pane.add(gridColorPicker, 3, row);
        TextField link1 = new TextField(Float.toString(starDescriptionPreference.getSize()));
        pane.add(link1, 4, row);

    }

}
