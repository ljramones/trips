package com.teamgannon.trips;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.AppViewPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RunExample extends Application
        implements ListUpdaterListener,
        RedrawListener,
        DatabaseListener,
        StellarPropertiesDisplayerListener,
        ContextSelectorListener,
        ReportGenerator,
        RouteUpdaterListener {

    private InterstellarSpacePane interstellarPaneExample;

    private final double sceneWidth = Universe.boxWidth;
    private final double sceneHeight = Universe.boxHeight;
    private final double depth = Universe.boxDepth;

    @Override
    public void start(Stage primaryStage) throws Exception {

        ColorPalette colorPalette = new ColorPalette();
        colorPalette.setDefaults();

        StarDisplayPreferences starDisplayPreferences = new StarDisplayPreferences();
        starDisplayPreferences.setDefaults();

        AppViewPreferences appViewPreferences = new AppViewPreferences();
        appViewPreferences.setColorPallete(colorPalette);
        appViewPreferences.setStarDisplayPreferences(starDisplayPreferences);

        TripsContext tripsContext = new TripsContext();
        tripsContext.setAppViewPreferences(appViewPreferences);

        double spacing = 20;

        interstellarPaneExample = new InterstellarSpacePane(sceneWidth,
                sceneHeight,
                depth,
                spacing,
                tripsContext,
                this,
                this,
                this,
                this,
                this,
                this,
                this
        );

        VBox controls = createControls();

        VBox vBox = new VBox(
                controls,
                interstellarPaneExample
        );

        vBox.layout();

        Scene scene = new Scene(vBox, sceneWidth, sceneHeight + 48);
        primaryStage.setTitle("2D Labels over 3D SubScene");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void plot(ActionEvent event) {
        interstellarPaneExample.simulateStars(35);
    }

    private VBox createControls() {
        Button plotButton = new Button("Button A");
        plotButton.setOnAction(this::plot);
        HBox hBox = new HBox(
                plotButton,
                new Button("Button B"),
                new Button("Button B")
        );
        hBox.setAlignment(Pos.CENTER);
        VBox controls = new VBox(10, hBox);
        controls.setPadding(new Insets(10));
        return controls;
    }

    public static void main(String[] args) {
        launch(args);
    }


    //////////////////////////// methods  ////////////////

    @Override
    public void selectInterstellarSpace(Map<String, String> objectProperties) {

    }

    @Override
    public void selectSolarSystemSpace(StarDisplayRecord starDisplayRecord) {

    }

    @Override
    public List<AstrographicObject> getAstrographicObjectsOnQuery() {
        return new ArrayList<>();
    }

    @Override
    public void updateStar(AstrographicObject astrographicObject) {

    }

    @Override
    public void updateNotesForStar(UUID recordId, String notes) {

    }

    @Override
    public AstrographicObject getStar(UUID starId) {
        return new AstrographicObject();
    }

    @Override
    public void removeStar(AstrographicObject astrographicObject) {

    }

    @Override
    public void removeStar(UUID id) {

    }

    @Override
    public void updateList(StarDisplayRecord starDisplayRecord) {

    }

    @Override
    public void clearList() {

    }

    @Override
    public void recenter(StarDisplayRecord starId) {

    }

    @Override
    public void highlightStar(UUID starId) {

    }

    @Override
    public void generateDistanceReport(StarDisplayRecord starDescriptor) {

    }

    @Override
    public void displayStellarProperties(AstrographicObject astrographicObject) {

    }

    @Override
    public void routingStatus(boolean statusFlag) {

    }

    @Override
    public void newRoute(DataSetDescriptor dataSetDescriptor, RouteDescriptor routeDescriptor) {

    }

    @Override
    public void updateRoute(RouteDescriptor routeDescriptor) {

    }

    @Override
    public void deleteRoute(RouteDescriptor routeDescriptor) {

    }

}
