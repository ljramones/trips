package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.StarObject;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

public class StarPropertiesPane extends Pane {

    private @NotNull StarObject record = new StarObject();

    // UI elements
    private final Label recordIdLabel = new Label();
    private final Label dataSetLabel = new Label();

    private final Label starNameLabel = new Label();
    private final ColorPicker starColorPicker = new ColorPicker();
    private final Label radiusLabel = new Label();
    private final Label distanceNameLabel = new Label();
    private final Label spectralClassLabel = new Label();
    private final Label tempLabel = new Label();

    private final Label xLabel = new Label();
    private final Label yLabel = new Label();
    private final Label zLabel = new Label();
    private final TextArea notesArea = new TextArea();

    //////////

    private final Label raLabel = new Label();
    private final Label pmraLabel = new Label();
    private final Label decLabel = new Label();
    private final Label pmdecLabel = new Label();
    private final Label decdegLabel = new Label();
    private final Label rsLabel = new Label();
    private final Label parallaxLabel = new Label();
    private final Label radialVelocityLabel = new Label();
    private final Label bprpLabel = new Label();
    private final Label bpgLabel = new Label();
    private final Label grpLabel = new Label();

    /////////
    private final Label polityLabel = new Label();
    private final Label worldTypeLabel = new Label();
    private final Label fuelTypeLabel = new Label();
    private final Label techTypeLabel = new Label();
    private final Label portTypeLabel = new Label();
    private final Label popTypeLabel = new Label();
    private final Label prodField = new Label();
    private final Label milspaceLabel = new Label();
    private final Label milplanLabel = new Label();

    private final CheckBox anomalyCheckbox = new CheckBox();
    private final CheckBox otherCheckbox = new CheckBox();

    public StarPropertiesPane() {

        VBox vBox = new VBox();

        TabPane tabPane = new TabPane();

        Tab overviewTab = new Tab("Overview");
        overviewTab.setContent(createOverviewTab());
        tabPane.getTabs().add(overviewTab);

        Tab secondaryTab = new Tab("Secondary");
        secondaryTab.setContent(createSecondaryTab());
        tabPane.getTabs().add(secondaryTab);

        Tab fictionalTab = new Tab("Fictional");
        fictionalTab.setContent(createFictionalTab());
        tabPane.getTabs().add(fictionalTab);

        vBox.getChildren().add(tabPane);

        this.getChildren().add(vBox);
    }

    public void setStar(@NotNull StarObject record) {
        this.record = record;

        // primary tab
        recordIdLabel.setText(record.getId().toString());
        dataSetLabel.setText(record.getDataSetName());
        starNameLabel.setText(record.getDisplayName());
        radiusLabel.setText(Double.toString(record.getRadius()));
        distanceNameLabel.setText(Double.toString(record.getDistance()));
        spectralClassLabel.setText(record.getSpectralClass());
        tempLabel.setText(Double.toString(record.getTemperature()));
        xLabel.setText(Double.toString(record.getX()));
        yLabel.setText(Double.toString(record.getY()));
        zLabel.setText(Double.toString(record.getZ()));
        notesArea.setText(record.getNotes());


        // secondary tab
        raLabel.setText(Double.toString(record.getRa()));
        pmraLabel.setText(Double.toString(record.getPmra()));
        decLabel.setText(Double.toString(record.getDeclination()));
        pmdecLabel.setText(Double.toString(record.getPmdec()));
        decdegLabel.setText(Double.toString(record.getDec_deg()));
        rsLabel.setText(Double.toString(record.getRs_cdeg()));
        parallaxLabel.setText(Double.toString(record.getParallax()));
        radialVelocityLabel.setText(Double.toString(record.getRadialVelocity()));
        bprpLabel.setText(Double.toString(record.getBprp()));
        bpgLabel.setText(Double.toString(record.getBpg()));
        grpLabel.setText(Double.toString(record.getGrp()));

        // third tab
        polityLabel.setText(record.getPolity());
        worldTypeLabel.setText(record.getWorldType());
        fuelTypeLabel.setText(record.getFuelType());
        techTypeLabel.setText(record.getTechType());
        portTypeLabel.setText(record.getPortType());
        popTypeLabel.setText(record.getPopulationType());
        prodField.setText(record.getProductType());
        milspaceLabel.setText(record.getMilSpaceType());
        milplanLabel.setText(record.getMilPlanType());
        anomalyCheckbox.setSelected(record.isAnomaly());
        otherCheckbox.setSelected(record.isOther());

    }

    private @NotNull Node createSecondaryTab() {
        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        
        // items for right grid
        gridPane.add(new Label("ra"), 0, 1);
        raLabel.setText(Double.toString(record.getRa()));
        gridPane.add(raLabel, 1, 1);

        gridPane.add(new Label("pmra"), 0, 2);
        pmraLabel.setText(Double.toString(record.getPmra()));
        gridPane.add(pmraLabel, 1, 2);

        gridPane.add(new Label("declination"), 0, 3);
        decLabel.setText(Double.toString(record.getDeclination()));
        gridPane.add(decLabel, 1, 3);

        gridPane.add(new Label("pmdec"), 0, 4);
        pmdecLabel.setText(Double.toString(record.getPmdec()));
        gridPane.add(pmdecLabel, 1, 4);

        gridPane.add(new Label("dec_deg"), 0, 5);
        decdegLabel.setText(Double.toString(record.getDec_deg()));
        gridPane.add(decdegLabel, 1, 5);

        gridPane.add(new Label("rs_cdeg"), 0, 6);
        rsLabel.setText(Double.toString(record.getRs_cdeg()));
        gridPane.add(rsLabel, 1, 6);

        gridPane.add(new Label("Parallax"), 0, 7);
        parallaxLabel.setText(Double.toString(record.getParallax()));
        gridPane.add(parallaxLabel, 1, 7);

        gridPane.add(new Label("Radial velocity"), 0, 8);
        radialVelocityLabel.setText(Double.toString(record.getRadialVelocity()));
        gridPane.add(radialVelocityLabel, 1, 8);

        gridPane.add(new Label("bprp"), 0, 9);
        bprpLabel.setText(Double.toString(record.getBprp()));
        gridPane.add(bprpLabel, 1, 9);

        gridPane.add(new Label("bpg"), 0, 10);
        bpgLabel.setText(Double.toString(record.getBpg()));
        gridPane.add(bpgLabel, 1, 10);

        gridPane.add(new Label("grp"), 0, 11);
        grpLabel.setText(Double.toString(record.getGrp()));
        gridPane.add(grpLabel, 1, 11);

        return gridPane;
    }

    private @NotNull Node createFictionalTab() {

        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        gridPane.add(new Label("polity"), 0, 1);
        polityLabel.setText(record.getPolity());
        gridPane.add(polityLabel, 1, 1);

        gridPane.add(new Label("world type"), 0, 2);
        worldTypeLabel.setText(record.getWorldType());
        gridPane.add(worldTypeLabel, 1, 2);

        gridPane.add(new Label("fuel type"), 0, 3);
        fuelTypeLabel.setText(record.getFuelType());
        gridPane.add(fuelTypeLabel, 1, 3);

        gridPane.add(new Label("tech type"), 0, 4);
        techTypeLabel.setText(record.getTechType());
        gridPane.add(techTypeLabel, 1, 4);

        gridPane.add(new Label("port type"), 0, 5);
        portTypeLabel.setText(record.getPortType());
        gridPane.add(portTypeLabel, 1, 5);

        gridPane.add(new Label("population type"), 0, 6);
        popTypeLabel.setText(record.getPopulationType());
        gridPane.add(popTypeLabel, 1, 6);

        gridPane.add(new Label("product type"), 0, 7);
        prodField.setText(record.getProductType());
        gridPane.add(prodField, 1, 7);

        gridPane.add(new Label("milspace type"), 0, 8);
        milspaceLabel.setText(record.getMilSpaceType());
        gridPane.add(milspaceLabel, 1, 8);

        gridPane.add(new Label("milplan type"), 0, 9);
        milplanLabel.setText(record.getMilPlanType());
        gridPane.add(milplanLabel, 1, 9);

        gridPane.add(new Label("anomaly"), 0, 10);
        anomalyCheckbox.setSelected(record.isAnomaly());
        gridPane.add(anomalyCheckbox, 1, 10);

        gridPane.add(new Label("other"), 0, 11);
        otherCheckbox.setSelected(record.isOther());
        gridPane.add(otherCheckbox, 1, 11);

        return gridPane;
    }

    private @NotNull Node createOverviewTab() {

        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        gridPane.add(new Label("Record id"), 0, 1);
        recordIdLabel.setText(record.getId().toString());
        gridPane.add(recordIdLabel, 1, 1);

        gridPane.add(new Label("Data set name"), 0, 2);
        dataSetLabel.setText(record.getDataSetName());
        gridPane.add(dataSetLabel, 1, 2);

        gridPane.add(new Label("Star name"), 0, 3);
        starNameLabel.setText(record.getDisplayName());
        gridPane.add(starNameLabel, 1, 3);

        gridPane.add(new Label("Radius"), 0, 5);
        radiusLabel.setText(Double.toString(record.getRadius()));
        gridPane.add(radiusLabel, 1, 5);

        gridPane.add(new Label("Distance"), 0, 6);
        distanceNameLabel.setText(Double.toString(record.getDistance()));
        gridPane.add(distanceNameLabel, 1, 6);

        gridPane.add(new Label("Spectral class"), 0, 7);
        spectralClassLabel.setText(record.getSpectralClass());
        gridPane.add(spectralClassLabel, 1, 7);

        gridPane.add(new Label("Temperature"), 0, 8);
        tempLabel.setText(Double.toString(record.getTemperature()));
        gridPane.add(tempLabel, 1, 8);

        gridPane.add(new Label("Coordinates"), 0, 9);
        GridPane coordGrid = new GridPane();
        gridPane.add(coordGrid, 1, 9);
        xLabel.setText(Double.toString(record.getX()));
        coordGrid.add(xLabel, 0, 1);
        yLabel.setText(Double.toString(record.getY()));
        coordGrid.add(yLabel, 1, 1);
        zLabel.setText(Double.toString(record.getZ()));
        coordGrid.add(zLabel, 2, 1);

        gridPane.add(new Label("Notes"), 0, 10);
        notesArea.setText(record.getNotes());
        notesArea.setDisable(true);
        gridPane.add(notesArea, 1, 10, 1, 3);

        return gridPane;
    }

}
