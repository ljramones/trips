package com.teamgannon.trips.floating;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.config.application.StarDisplayPreferences;
import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.graphics.CurrentPlot;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.jpa.model.AstrographicObject;
import com.teamgannon.trips.listener.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RunExample extends Application
implements ListUpdaterListener,
        RedrawListener,
        DatabaseListener,
        StellarPropertiesDisplayerListener,
        ContextSelectorListener,
        ReportGenerator {


    @Override
    public void start(Stage primaryStage) throws Exception {

        double sceneWidth = Universe.boxWidth;
        double sceneHeight = Universe.boxHeight;
        double depth = Universe.boxDepth;

        ColorPalette colorPalette = new ColorPalette();
        colorPalette.setDefaults();

        double spacing = 20;

        InterstellarExample interstellarExample = new InterstellarExample(
                sceneWidth,
                sceneHeight,
                depth,
                spacing,
                this,
                this,
                this,
                this,
                this,
                new StarDisplayPreferences(),
                this,
                new CurrentPlot(),
                colorPalette
        );

        Scene scene = new Scene(interstellarExample.getRoot(), sceneWidth, sceneHeight);
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
        return null;
    }

    @Override
    public void updateStar(AstrographicObject astrographicObject) {

    }

    @Override
    public void updateNotesForStar(UUID recordId, String notes) {

    }

    @Override
    public AstrographicObject getStar(UUID starId) {
        return null;
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
}
