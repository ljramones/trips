package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.StarObject;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class StarPropertiesPane extends Pane {

    // UI elements
    private final Label starNameLabel = new Label();
    private final Label constellationNameLabel = new Label();
    private final ColorPicker starColorPicker = new ColorPicker();
    private final Label radiusLabel = new Label();
    private final Label distanceNameLabel = new Label();
    private final Label spectralClassLabel = new Label();
    private final Label tempLabel = new Label();
    private final Label raLabel = new Label();
    private final Label commonNameLabel = new Label();
    private final TextArea notesArea = new TextArea();

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

    //////////

    private final Label simbadIdLabel = new Label();
    private final Label galacticCoordinatesLabel = new Label();
    private final Label pmraLabel = new Label();
    private final Label decLabel = new Label();
    private final Label pmdecLabel = new Label();
    private final Label radialVelocityLabel = new Label();
    private final Label parallaxLabel = new Label();

    private final Label maguLabel = new Label();
    private final Label magbLabel = new Label();
    private final Label magvLabel = new Label();
    private final Label magrLabel = new Label();
    private final Label magiLabel = new Label();

    private final Label bprpLabel = new Label();
    private final Label bpgLabel = new Label();
    private final Label grpLabel = new Label();


    private @NotNull StarObject record = new StarObject();
    private HostServices hostServices;

    public StarPropertiesPane(HostServices hostServices) {
        this.hostServices = hostServices;

        VBox vBox = new VBox();

        TabPane tabPane = new TabPane();

        Tab overviewTab = new Tab("Overview");
        overviewTab.setContent(createOverviewTab());
        tabPane.getTabs().add(overviewTab);

        Tab fictionalTab = new Tab("Fictional");
        fictionalTab.setContent(createFictionalTab());
        tabPane.getTabs().add(fictionalTab);

        Tab secondaryTab = new Tab("Secondary");
        secondaryTab.setContent(createSecondaryTab());
        tabPane.getTabs().add(secondaryTab);

        vBox.getChildren().add(tabPane);

        this.getChildren().add(vBox);
    }

    public void setStar(@NotNull StarObject record) {
        this.record = record;

        // primary tab
        starNameLabel.setText(record.getDisplayName());
        constellationNameLabel.setText(record.getConstellationName());
        spectralClassLabel.setText(record.getOrthoSpectralClass());
        distanceNameLabel.setText(Double.toString(record.getDistance()));
        tempLabel.setText(Double.toString(record.getTemperature()));
        radiusLabel.setText(Double.toString(record.getRadius()));
        raLabel.setText(Double.toString(record.getRa()));
        decLabel.setText(Double.toString(record.getDeclination()));
        commonNameLabel.setText(record.getMiscText1());
        notesArea.setText(record.getNotes());

        // fictional tab
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

        // secondary tab
        simbadIdLabel.setText(record.getMiscText2());
        galacticCoordinatesLabel.setText(record.getMiscText4());
        pmraLabel.setText(Double.toString(record.getPmra()));
        pmdecLabel.setText(Double.toString(record.getPmdec()));
        radialVelocityLabel.setText(Double.toString(record.getRadialVelocity()));
        parallaxLabel.setText(Double.toString(record.getParallax()));

        maguLabel.setText(Double.toString(record.getMagu()));
        magbLabel.setText(Double.toString(record.getMagb()));
        magvLabel.setText(Double.toString(record.getMagv()));
        magrLabel.setText(Double.toString(record.getMagr()));
        magiLabel.setText(Double.toString(record.getMagi()));

        bprpLabel.setText(Double.toString(record.getBprp()));
        bpgLabel.setText(Double.toString(record.getBpg()));
        grpLabel.setText(Double.toString(record.getGrp()));

    }


    private @NotNull Node createOverviewTab() {

        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        gridPane.add(new Label("Star name"), 0, 1);
        starNameLabel.setText(record.getDisplayName());
        gridPane.add(starNameLabel, 1, 1);

        gridPane.add(new Label("Constellation"), 0, 2);
        constellationNameLabel.setText(record.getConstellationName());
        gridPane.add(constellationNameLabel, 1, 2);

        gridPane.add(new Label("Spectral class"), 0, 3);
        spectralClassLabel.setText(record.getSpectralClass());
        gridPane.add(spectralClassLabel, 1, 3);

        gridPane.add(new Label("Distance"), 0, 4);
        distanceNameLabel.setText(Double.toString(record.getDistance()));
        gridPane.add(distanceNameLabel, 1, 4);

        gridPane.add(new Label("Temperature"), 0, 5);
        tempLabel.setText(Double.toString(record.getTemperature()));
        gridPane.add(tempLabel, 1, 5);

        gridPane.add(new Label("Radius"), 0, 6);
        radiusLabel.setText(Double.toString(record.getRadius()));
        gridPane.add(radiusLabel, 1, 6);

        gridPane.add(new Label("Ra"), 0, 7);
        raLabel.setText(Double.toString(record.getRa()));
        gridPane.add(raLabel, 1, 7);

        gridPane.add(new Label("Declination"), 0, 8);
        decLabel.setText(Double.toString(record.getDeclination()));
        gridPane.add(decLabel, 1, 8);

        gridPane.add(new Label("Common name"), 0, 9);
        commonNameLabel.setText(record.getMiscText1());
        gridPane.add(commonNameLabel, 1, 9);

        gridPane.add(new Label("Notes"), 0, 10);
        notesArea.setText(record.getNotes());
        notesArea.setDisable(true);
        gridPane.add(notesArea, 1, 10, 1, 10);

        return gridPane;
    }

    private @NotNull Node createFictionalTab() {

        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        gridPane.add(new Label("Polity"), 0, 1);
        polityLabel.setText(record.getPolity());
        gridPane.add(polityLabel, 1, 1);

        gridPane.add(new Label("World type"), 0, 2);
        worldTypeLabel.setText(record.getWorldType());
        gridPane.add(worldTypeLabel, 1, 2);

        gridPane.add(new Label("Fuel type"), 0, 3);
        fuelTypeLabel.setText(record.getFuelType());
        gridPane.add(fuelTypeLabel, 1, 3);

        gridPane.add(new Label("Tech type"), 0, 4);
        techTypeLabel.setText(record.getTechType());
        gridPane.add(techTypeLabel, 1, 4);

        gridPane.add(new Label("Port type"), 0, 5);
        portTypeLabel.setText(record.getPortType());
        gridPane.add(portTypeLabel, 1, 5);

        gridPane.add(new Label("Population type"), 0, 6);
        popTypeLabel.setText(record.getPopulationType());
        gridPane.add(popTypeLabel, 1, 6);

        gridPane.add(new Label("Product type"), 0, 7);
        prodField.setText(record.getProductType());
        gridPane.add(prodField, 1, 7);

        gridPane.add(new Label("Milspace type"), 0, 8);
        milspaceLabel.setText(record.getMilSpaceType());
        gridPane.add(milspaceLabel, 1, 8);

        gridPane.add(new Label("Milplan type"), 0, 9);
        milplanLabel.setText(record.getMilPlanType());
        gridPane.add(milplanLabel, 1, 9);

        gridPane.add(new Label("Anomaly"), 0, 10);
        anomalyCheckbox.setSelected(record.isAnomaly());
        gridPane.add(anomalyCheckbox, 1, 10);

        gridPane.add(new Label("Other"), 0, 11);
        otherCheckbox.setSelected(record.isOther());
        gridPane.add(otherCheckbox, 1, 11);

        return gridPane;
    }

    private @NotNull Node createSecondaryTab() {
        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

//        HostServices hostServices = (HostServices) context.getBean("HostServices");

        // items for right grid

        gridPane.add(new Label("Simbad Id"), 0, 1);
        simbadIdLabel.setText(record.getMiscText2());
        gridPane.add(simbadIdLabel, 1, 1);
        Button simbadButton = new Button("Info");
        simbadButton.setOnAction(event -> {
            String simbadRecord = URLEncoder.encode(record.getMiscText2(), StandardCharsets.UTF_8);
            hostServices.showDocument("http://simbad.u-strasbg.fr/simbad/sim-id?Ident=" + simbadRecord);
        });
        gridPane.add(simbadButton, 2, 1);

        gridPane.add(new Label("Galactic coordinates"), 0, 2);
        galacticCoordinatesLabel.setText(record.getMiscText4());
        gridPane.add(galacticCoordinatesLabel, 1, 2);

        gridPane.add(new Label("Pmra"), 0, 3);
        pmraLabel.setText(Double.toString(record.getPmra()));
        gridPane.add(pmraLabel, 1, 3);

        gridPane.add(new Label("Pmdec"), 0, 4);
        pmdecLabel.setText(Double.toString(record.getPmdec()));
        gridPane.add(pmdecLabel, 1, 4);

        gridPane.add(new Label("Radial velocity"), 0, 5);
        radialVelocityLabel.setText(Double.toString(record.getRadialVelocity()));
        gridPane.add(radialVelocityLabel, 1, 5);

        gridPane.add(new Label("Parallax"), 0, 6);
        parallaxLabel.setText(Double.toString(record.getParallax()));
        gridPane.add(parallaxLabel, 1, 6);

        gridPane.add(new Label("Magu"), 0, 7);
        maguLabel.setText(Double.toString(record.getMagu()));
        gridPane.add(maguLabel, 1, 7);

        gridPane.add(new Label("Magb"), 0, 8);
        magbLabel.setText(Double.toString(record.getMagb()));
        gridPane.add(magbLabel, 1, 8);

        gridPane.add(new Label("Magv"), 0, 9);
        magvLabel.setText(Double.toString(record.getMagv()));
        gridPane.add(magvLabel, 1, 9);

        gridPane.add(new Label("Magr"), 0, 10);
        magrLabel.setText(Double.toString(record.getMagr()));
        gridPane.add(magrLabel, 1, 10);

        gridPane.add(new Label("Magi"), 0, 11);
        magiLabel.setText(Double.toString(record.getMagi()));
        gridPane.add(magiLabel, 1, 11);

        gridPane.add(new Label("Bprp"), 0, 12);
        bprpLabel.setText(Double.toString(record.getBprp()));
        gridPane.add(bprpLabel, 1, 12);

        gridPane.add(new Label("Bpg"), 0, 13);
        bpgLabel.setText(Double.toString(record.getBpg()));
        gridPane.add(bpgLabel, 1, 13);

        gridPane.add(new Label("Grp"), 0, 14);
        grpLabel.setText(Double.toString(record.getGrp()));
        gridPane.add(grpLabel, 1, 14);

        return gridPane;
    }

}
