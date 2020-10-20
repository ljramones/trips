package com.teamgannon.trips.floating;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.TripsContext;
import com.teamgannon.trips.config.application.model.AppViewPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.entities.RouteDescriptor;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.*;
import javafx.application.Application;
import javafx.scene.Scene;
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


    @Override
    public void start(Stage primaryStage) throws Exception {

        double sceneWidth = Universe.boxWidth;
        double sceneHeight = Universe.boxHeight;
        double depth = Universe.boxDepth;

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

        InterstellarPaneExample interstellarPaneExample = new InterstellarPaneExample(sceneWidth,
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

        Scene scene = new Scene(interstellarPaneExample.getRoot(), sceneWidth, sceneHeight);
        primaryStage.setTitle("2D Labels over 3D SubScene");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

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
