package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.jpa.model.StarObject;
import javafx.application.HostServices;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class StarPropertiesPane extends Pane {

    // Overview
    private final Label starNameLabel1 = new Label();
    private final Label commonNameLabel1 = new Label();
    private final Label constellationNameLabel = new Label();
    private final Label spectralClassLabel = new Label();
    private final Label distanceNameLabel = new Label();
    private final Label metallicityLabel = new Label();
    private final Label ageLabel = new Label();
    private final TextArea notesArea = new TextArea();

    // fictional info
    private final Label starNameLabel2 = new Label();
    private final Label commonNameLabel2 = new Label();
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

    // Other Info
    private final Label starNameLabel3 = new Label();
    private final Label commonNameLabel3 = new Label();
    private final Label simbadIdLabel = new Label();
    private final Label galacticCoordinatesLabel = new Label();
    private final Label radiusLabel = new Label();
    private final Label raLabel = new Label();
    private final Label decLabel = new Label();
    private final Label pmraLabel = new Label();
    private final Label pmdecLabel = new Label();
    private final Label radialVelocityLabel = new Label();
    private final Label parallaxLabel = new Label();
    private final Label tempLabel = new Label();


    private final Label maguLabel = new Label();
    private final Label magbLabel = new Label();
    private final Label magvLabel = new Label();
    private final Label magrLabel = new Label();
    private final Label magiLabel = new Label();

    private final Label bprpLabel = new Label();
    private final Label bpgLabel = new Label();
    private final Label grpLabel = new Label();

    private Button simbadButton = new Button("More\nInfo");


    private @NotNull StarObject record = new StarObject();
    private final HostServices hostServices;

    public StarPropertiesPane(HostServices hostServices) {
        this.hostServices = hostServices;

        VBox vBox = new VBox();

        TabPane tabPane = new TabPane();

        Tab overviewTab = new Tab("Overview");
        overviewTab.setContent(createOverviewTab());
        tabPane.getTabs().add(overviewTab);

        Tab fictionalTab = new Tab("Fictional Info");
        fictionalTab.setContent(createFictionalTab());
        tabPane.getTabs().add(fictionalTab);

        Tab secondaryTab = new Tab("Other Info");
        secondaryTab.setContent(createOtherInfo());
        tabPane.getTabs().add(secondaryTab);

        vBox.getChildren().add(tabPane);
        this.getChildren().add(vBox);
    }

    public void setStar(@NotNull StarObject record) {
        this.record = record;

        // primary tab
        starNameLabel1.setText(record.getDisplayName());
        commonNameLabel1.setText(record.getCommonName());
        constellationNameLabel.setText(record.getConstellationName());
        spectralClassLabel.setText(record.getOrthoSpectralClass());
        distanceNameLabel.setText(Double.toString(record.getDistance()));
        metallicityLabel.setText(Double.toString(record.getMetallicity()));
        ageLabel.setText(Double.toString(record.getAge()));
        notesArea.setText(record.getNotes());

        // fictional info tab
        starNameLabel2.setText(record.getDisplayName());
        commonNameLabel2.setText(record.getCommonName());
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

        // other info tab
        starNameLabel3.setText(record.getDisplayName());
        commonNameLabel3.setText(record.getCommonName());
        simbadIdLabel.setText(record.getSimbadId());
        galacticCoordinatesLabel.setText(record.getGalacticLat() + "' " + record.getGalacticLong());
        radiusLabel.setText(Double.toString(record.getRadius()));
        raLabel.setText(Double.toString(record.getRa()));
        decLabel.setText(Double.toString(record.getDeclination()));
        pmraLabel.setText(Double.toString(record.getPmra()));
        pmdecLabel.setText(Double.toString(record.getPmdec()));
        radialVelocityLabel.setText(Double.toString(record.getRadialVelocity()));
        parallaxLabel.setText(Double.toString(record.getParallax()));
        tempLabel.setText(Double.toString(record.getTemperature()));

        maguLabel.setText(Double.toString(record.getMagu()));
        magbLabel.setText(Double.toString(record.getMagb()));
        magvLabel.setText(Double.toString(record.getMagv()));
        magrLabel.setText(Double.toString(record.getMagr()));
        magiLabel.setText(Double.toString(record.getMagi()));

        bprpLabel.setText(Double.toString(record.getBprp()));
        bpgLabel.setText(Double.toString(record.getBpg()));
        grpLabel.setText(Double.toString(record.getGrp()));

        simbadButton.setDisable(record.getSimbadId().isEmpty());

    }

    public void clearData() {

        // primary tab
        starNameLabel1.setText("");
        commonNameLabel1.setText("");
        constellationNameLabel.setText("");
        spectralClassLabel.setText("");
        distanceNameLabel.setText("");
        metallicityLabel.setText("");
        ageLabel.setText("");
        notesArea.setText("");

        // fictional info tab
        starNameLabel2.setText("");
        commonNameLabel2.setText("");
        polityLabel.setText("");
        worldTypeLabel.setText("");
        fuelTypeLabel.setText("");
        techTypeLabel.setText("");
        portTypeLabel.setText("");
        popTypeLabel.setText("");
        prodField.setText("");
        milspaceLabel.setText("");
        milplanLabel.setText("");
        anomalyCheckbox.setSelected(false);
        otherCheckbox.setSelected(false);

        // other info tab
        starNameLabel3.setText("");
        commonNameLabel3.setText("");
        simbadIdLabel.setText("");
        galacticCoordinatesLabel.setText("");
        radiusLabel.setText("");
        raLabel.setText("");
        decLabel.setText("");
        pmraLabel.setText("");
        pmdecLabel.setText("");
        radialVelocityLabel.setText("");
        parallaxLabel.setText("");
        tempLabel.setText("");

        maguLabel.setText("");
        magbLabel.setText("");
        magvLabel.setText("");
        magrLabel.setText("");
        magiLabel.setText("");

        bprpLabel.setText("");
        bpgLabel.setText("");
        grpLabel.setText("");

        simbadButton.setDisable(true);
    }


    private @NotNull Node createOverviewTab() {

        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        gridPane.add(new Label("Star name"), 0, 1);
        starNameLabel1.setText(record.getDisplayName());
        gridPane.add(starNameLabel1, 1, 1);

        gridPane.add(new Label("Common name"), 0, 2);
        commonNameLabel1.setText(record.getCommonName());
        gridPane.add(commonNameLabel1, 1, 2);

        gridPane.add(new Label("Constellation"), 0, 3);
        constellationNameLabel.setText(record.getConstellationName());
        gridPane.add(constellationNameLabel, 1, 3);

        gridPane.add(new Label("Spectral class"), 0, 4);
        spectralClassLabel.setText(record.getSpectralClass());
        gridPane.add(spectralClassLabel, 1, 4);

        gridPane.add(new Label("Distance"), 0, 5);
        distanceNameLabel.setText(Double.toString(record.getDistance()));
        gridPane.add(distanceNameLabel, 1, 5);

        gridPane.add(new Label("Metallicity"), 0, 6);
        metallicityLabel.setText(Double.toString(record.getMetallicity()));
        gridPane.add(metallicityLabel, 1, 6);

        gridPane.add(new Label("Age"), 0, 7);
        ageLabel.setText(Double.toString(record.getAge()));
        gridPane.add(ageLabel, 1, 7);

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

        gridPane.add(new Label("Star name"), 0, 1);
        starNameLabel2.setText(record.getDisplayName());
        gridPane.add(starNameLabel2, 1, 1);

        gridPane.add(new Label("Common name"), 0, 2);
        commonNameLabel2.setText(record.getCommonName());
        gridPane.add(commonNameLabel2, 1, 2);

        gridPane.add(new Label("Polity"), 0, 3);
        polityLabel.setText(record.getPolity());
        gridPane.add(polityLabel, 1, 3);

        gridPane.add(new Label("World type"), 0, 4);
        worldTypeLabel.setText(record.getWorldType());
        gridPane.add(worldTypeLabel, 1, 4);

        gridPane.add(new Label("Fuel type"), 0, 5);
        fuelTypeLabel.setText(record.getFuelType());
        gridPane.add(fuelTypeLabel, 1, 5);

        gridPane.add(new Label("Tech type"), 0, 6);
        techTypeLabel.setText(record.getTechType());
        gridPane.add(techTypeLabel, 1, 6);

        gridPane.add(new Label("Port type"), 0, 7);
        portTypeLabel.setText(record.getPortType());
        gridPane.add(portTypeLabel, 1, 7);

        gridPane.add(new Label("Population type"), 0, 8);
        popTypeLabel.setText(record.getPopulationType());
        gridPane.add(popTypeLabel, 1, 8);

        gridPane.add(new Label("Product type"), 0, 9);
        prodField.setText(record.getProductType());
        gridPane.add(prodField, 1, 9);

        gridPane.add(new Label("Milspace type"), 0, 10);
        milspaceLabel.setText(record.getMilSpaceType());
        gridPane.add(milspaceLabel, 1, 10);

        gridPane.add(new Label("Milplan type"), 0, 11);
        milplanLabel.setText(record.getMilPlanType());
        gridPane.add(milplanLabel, 1, 11);

        gridPane.add(new Label("Anomaly"), 0, 12);
        anomalyCheckbox.setSelected(record.isAnomaly());
        gridPane.add(anomalyCheckbox, 1, 12);

        gridPane.add(new Label("Other"), 0, 13);
        otherCheckbox.setSelected(record.isOther());
        gridPane.add(otherCheckbox, 1, 13);

        return gridPane;
    }

    private @NotNull Node createOtherInfo() {
        // setup grid structure
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);

        gridPane.add(new Label("Star name"), 0, 1);
        starNameLabel3.setText(record.getDisplayName());
        gridPane.add(starNameLabel3, 1, 1);

        gridPane.add(new Label("Common name"), 0, 2);
        commonNameLabel3.setText(record.getCommonName());
        gridPane.add(commonNameLabel3, 1, 2);

        HBox hBox = new HBox();
        gridPane.add(new Label("Simbad Id"), 0, 3);
        simbadIdLabel.setText(record.getMiscText2());
        hBox.getChildren().add(simbadIdLabel);
        hBox.getChildren().add(new Label("    "));

        simbadButton.setOnAction(event -> {
            String simbadRecord = URLEncoder.encode(record.getSimbadId(), StandardCharsets.UTF_8);
            hostServices.showDocument("http://simbad.u-strasbg.fr/simbad/sim-id?Ident=" + simbadRecord);
        });
        if (record.getSimbadId().isEmpty()) {
            simbadButton.setDisable(true);
        } else {
            simbadButton.setDisable(false);
        }
        hBox.getChildren().add(simbadButton);
        gridPane.add(hBox, 1, 3);

        gridPane.add(new Label("Galactic coordinates"), 0, 4);
        galacticCoordinatesLabel.setText(record.getGalacticLat() + ", " + record.getGalacticLong());
        gridPane.add(galacticCoordinatesLabel, 1, 4);

        gridPane.add(new Label("Radius"), 0, 5);
        radiusLabel.setText(Double.toString(record.getRadius()));
        gridPane.add(radiusLabel, 1, 5);

        gridPane.add(new Label("Ra"), 0, 6);
        raLabel.setText(Double.toString(record.getRa()));
        gridPane.add(raLabel, 1, 6);

        gridPane.add(new Label("Declination"), 0, 7);
        decLabel.setText(Double.toString(record.getDeclination()));
        gridPane.add(decLabel, 1, 7);

        gridPane.add(new Label("Pmra"), 0, 8);
        pmraLabel.setText(Double.toString(record.getPmra()));
        gridPane.add(pmraLabel, 1, 8);

        gridPane.add(new Label("Pmdec"), 0, 9);
        pmdecLabel.setText(Double.toString(record.getPmdec()));
        gridPane.add(pmdecLabel, 1, 9);

        gridPane.add(new Label("Radial velocity"), 0, 10);
        radialVelocityLabel.setText(Double.toString(record.getRadialVelocity()));
        gridPane.add(radialVelocityLabel, 1, 10);

        gridPane.add(new Label("Parallax"), 0, 11);
        parallaxLabel.setText(Double.toString(record.getParallax()));
        gridPane.add(parallaxLabel, 1, 11);

        gridPane.add(new Label("Temperature"), 0, 12);
        tempLabel.setText(Double.toString(record.getTemperature()));
        gridPane.add(tempLabel, 1, 12);


        gridPane.add(new Label("Magu"), 0, 13);
        maguLabel.setText(Double.toString(record.getMagu()));
        gridPane.add(maguLabel, 1, 13);

        gridPane.add(new Label("Magb"), 0, 14);
        magbLabel.setText(Double.toString(record.getMagb()));
        gridPane.add(magbLabel, 1, 14);

        gridPane.add(new Label("Magv"), 0, 15);
        magvLabel.setText(Double.toString(record.getMagv()));
        gridPane.add(magvLabel, 1, 15);

        gridPane.add(new Label("Magr"), 0, 16);
        magrLabel.setText(Double.toString(record.getMagr()));
        gridPane.add(magrLabel, 1, 16);

        gridPane.add(new Label("Magi"), 0, 17);
        magiLabel.setText(Double.toString(record.getMagi()));
        gridPane.add(magiLabel, 1, 17);

        gridPane.add(new Label("Bprp"), 0, 18);
        bprpLabel.setText(Double.toString(record.getBprp()));
        gridPane.add(bprpLabel, 1, 18);

        gridPane.add(new Label("Bpg"), 0, 19);
        bpgLabel.setText(Double.toString(record.getBpg()));
        gridPane.add(bpgLabel, 1, 19);

        gridPane.add(new Label("Grp"), 0, 20);
        grpLabel.setText(Double.toString(record.getGrp()));
        gridPane.add(grpLabel, 1, 20);

        return gridPane;
    }

}
