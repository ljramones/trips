package com.teamgannon.trips.screenobjects;

import com.teamgannon.trips.events.ClearDataEvent;
import com.teamgannon.trips.events.DisplayStarEvent;
import com.teamgannon.trips.jpa.model.StarObject;
import com.teamgannon.trips.service.StarService;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Optional;

@Slf4j
@Component
public class StarPropertiesPane extends VBox {

    // Overview
    @FXML
    private Label starNameLabel1;
    @FXML
    private Label commonNameLabel1;
    @FXML
    private Label constellationNameLabel;
    @FXML
    private Label spectralClassLabel;
    @FXML
    private Label distanceNameLabel;
    @FXML
    private Label metallicityLabel;
    @FXML
    private Label ageLabel;
    @FXML
    private TextArea notesArea;

    // fictional info
    @FXML
    private Label starNameLabel2;
    @FXML
    private Label commonNameLabel2;
    @FXML
    private Label polityLabel;
    @FXML
    private Label worldTypeLabel;
    @FXML
    private Label fuelTypeLabel;
    @FXML
    private Label techTypeLabel;
    @FXML
    private Label portTypeLabel;
    @FXML
    private Label popTypeLabel;
    @FXML
    private Label prodField;
    @FXML
    private Label milspaceLabel;
    @FXML
    private Label milplanLabel;
    @FXML
    private CheckBox anomalyCheckbox;
    @FXML
    private CheckBox otherCheckbox;

    // Other Info
    @FXML
    private Label starNameLabel3;
    @FXML
    private Label commonNameLabel3;
    @FXML
    private Label simbadIdLabel;
    @FXML
    private Label galacticCoordinatesLabel;
    @FXML
    private Label radiusLabel;
    @FXML
    private Label raLabel;
    @FXML
    private Label decLabel;
    @FXML
    private Label pmraLabel;
    @FXML
    private Label pmdecLabel;
    @FXML
    private Label radialVelocityLabel;
    @FXML
    private Label parallaxLabel;
    @FXML
    private Label tempLabel;

    @FXML
    private Label maguLabel;
    @FXML
    private Label magbLabel;
    @FXML
    private Label magvLabel;
    @FXML
    private Label magrLabel;
    @FXML
    private Label magiLabel;

    @FXML
    private Label bprpLabel;
    @FXML
    private Label bpgLabel;
    @FXML
    private Label grpLabel;

    @FXML
    private Button editButton;
    @FXML
    private Button simbadButton;


    private @NotNull StarObject record = new StarObject();
    private final StarService starService;
    private final HostServices hostServices;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.###");

    public StarPropertiesPane(StarService starService,
                              FxWeaver fxWeaver) {
        this.starService = starService;
        this.hostServices = fxWeaver.getBean(HostServices.class);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("StarPropertiesPane.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load StarPropertiesPane.fxml", ex);
        }
    }

    @FXML
    private void initialize() {
        editButton.setDisable(true);
        editButton.setOnAction(this::editStar);
        simbadButton.setOnAction(event -> openSimbad());
    }

    private void editStar(ActionEvent actionEvent) {
        if (record.getId() != null) {
            log.info("edit star");
            StarEditDialog starEditDialog = new StarEditDialog(record);
            Optional<StarEditStatus> statusOptional = starEditDialog.showAndWait();
            if (statusOptional.isPresent()) {
                StarEditStatus starEditStatus = statusOptional.get();
                if (starEditStatus.isChanged()) {
                    // update the database
                    starService.updateStar(starEditStatus.getRecord());
                    setStar(starEditStatus.getRecord());
                    this.getScene().getWindow().setWidth(this.getScene().getWindow().getWidth() + 0.001);
                }
            }
        }
    }

    public void setStar(@NotNull StarObject record) {
        this.record = record;

        editButton.setDisable(false);

        // primary tab
        starNameLabel1.setText(safeDisplay(record.getDisplayName()));
        commonNameLabel1.setText(safeDisplay(record.getCommonName()));
        constellationNameLabel.setText(safeDisplay(record.getConstellationName()));
        spectralClassLabel.setText(safeDisplay(record.getOrthoSpectralClass()));
        distanceNameLabel.setText(formatDouble(record.getDistance()));
        metallicityLabel.setText(formatDouble(record.getMetallicity()));
        ageLabel.setText(formatDouble(record.getAge()));
        notesArea.setText(safeDisplay(record.getNotes()));

        // fictional info tab
        starNameLabel2.setText(safeDisplay(record.getDisplayName()));
        commonNameLabel2.setText(safeDisplay(record.getCommonName()));
        polityLabel.setText(safeDisplay(record.getPolity()));
        worldTypeLabel.setText(safeDisplay(record.getWorldType()));
        fuelTypeLabel.setText(safeDisplay(record.getFuelType()));
        techTypeLabel.setText(safeDisplay(record.getTechType()));
        portTypeLabel.setText(safeDisplay(record.getPortType()));
        popTypeLabel.setText(safeDisplay(record.getPopulationType()));
        prodField.setText(safeDisplay(record.getProductType()));
        milspaceLabel.setText(safeDisplay(record.getMilSpaceType()));
        milplanLabel.setText(safeDisplay(record.getMilPlanType()));
        anomalyCheckbox.setSelected(record.isAnomaly());
        otherCheckbox.setSelected(record.isOther());

        // other info tab
        starNameLabel3.setText(safeDisplay(record.getDisplayName()));
        commonNameLabel3.setText(safeDisplay(record.getCommonName()));
        String simbadId = safeDisplay(record.getSimbadId());
        simbadIdLabel.setText(simbadId);
        galacticCoordinatesLabel.setText(formatDouble(record.getGalacticLat()) + ", " + formatDouble(record.getGalacticLong()));
        radiusLabel.setText(formatDouble(record.getRadius()));
        raLabel.setText(formatDouble(record.getRa()));
        decLabel.setText(formatDouble(record.getDeclination()));
        pmraLabel.setText(formatDouble(record.getPmra()));
        pmdecLabel.setText(formatDouble(record.getPmdec()));
        radialVelocityLabel.setText(formatDouble(record.getRadialVelocity()));
        parallaxLabel.setText(formatDouble(record.getParallax()));
        tempLabel.setText(formatDouble(record.getTemperature()));

        maguLabel.setText(formatDouble(record.getMagu()));
        magbLabel.setText(formatDouble(record.getMagb()));
        magvLabel.setText(formatDouble(record.getMagv()));
        magrLabel.setText(formatDouble(record.getMagr()));
        magiLabel.setText(formatDouble(record.getMagi()));

        bprpLabel.setText(formatDouble(record.getBprp()));
        bpgLabel.setText(formatDouble(record.getBpg()));
        grpLabel.setText(formatDouble(record.getGrp()));

        simbadButton.setDisable(simbadId.isEmpty() || emptyDisplay().equals(simbadId));
    }

    /**
     * Clears the data displayed in the UI components.
     */
    public void clearData() {

        // primary tab
        starNameLabel1.setText(emptyDisplay());
        commonNameLabel1.setText(emptyDisplay());
        constellationNameLabel.setText(emptyDisplay());
        spectralClassLabel.setText(emptyDisplay());
        distanceNameLabel.setText(emptyDisplay());
        metallicityLabel.setText(emptyDisplay());
        ageLabel.setText(emptyDisplay());
        notesArea.setText("");

        // fictional info tab
        starNameLabel2.setText(emptyDisplay());
        commonNameLabel2.setText(emptyDisplay());
        polityLabel.setText(emptyDisplay());
        worldTypeLabel.setText(emptyDisplay());
        fuelTypeLabel.setText(emptyDisplay());
        techTypeLabel.setText(emptyDisplay());
        portTypeLabel.setText(emptyDisplay());
        popTypeLabel.setText(emptyDisplay());
        prodField.setText(emptyDisplay());
        milspaceLabel.setText(emptyDisplay());
        milplanLabel.setText(emptyDisplay());
        anomalyCheckbox.setSelected(false);
        otherCheckbox.setSelected(false);

        // other info tab
        starNameLabel3.setText(emptyDisplay());
        commonNameLabel3.setText(emptyDisplay());
        simbadIdLabel.setText(emptyDisplay());
        galacticCoordinatesLabel.setText(emptyDisplay());
        radiusLabel.setText(emptyDisplay());
        raLabel.setText(emptyDisplay());
        decLabel.setText(emptyDisplay());
        pmraLabel.setText(emptyDisplay());
        pmdecLabel.setText(emptyDisplay());
        radialVelocityLabel.setText(emptyDisplay());
        parallaxLabel.setText(emptyDisplay());
        tempLabel.setText(emptyDisplay());

        maguLabel.setText(emptyDisplay());
        magbLabel.setText(emptyDisplay());
        magvLabel.setText(emptyDisplay());
        magrLabel.setText(emptyDisplay());
        magiLabel.setText(emptyDisplay());

        bprpLabel.setText(emptyDisplay());
        bpgLabel.setText(emptyDisplay());
        grpLabel.setText(emptyDisplay());

        simbadButton.setDisable(true);
    }

    private void openSimbad() {
        String simbadId = safeDisplay(record.getSimbadId());
        if (simbadId.isEmpty() || emptyDisplay().equals(simbadId)) {
            return;
        }
        String simbadRecord = URLEncoder.encode(simbadId, StandardCharsets.UTF_8);
        hostServices.showDocument("http://simbad.u-strasbg.fr/simbad/sim-id?Ident=" + simbadRecord);
    }

    private String formatDouble(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return emptyDisplay();
        }
        return decimalFormat.format(value);
    }

    private static String safeDisplay(String value) {
        if (value == null || value.isBlank()) {
            return emptyDisplay();
        }
        return value;
    }

    private static String emptyDisplay() {
        return "--";
    }


    /**
     * Listens for the ClearDataEvent and clears the data asynchronously on the JavaFX Application Thread.
     * This method is annotated with EventListener to indicate that it is an event listener for the ClearDataEvent.
     * It uses the Platform.runLater() method to execute the clearData() method on the JavaFX Application Thread.
     * <p>
     * // Trigger the ClearDataEvent
     * EventManager.triggerEvent(new ClearDataEvent());
     * <p>
     * // The onClearDataEvent() method will be automatically called on the JavaFX Application Thread
     * // and the data will be cleared.
     */
    @EventListener
    public void onClearDataEvent(ClearDataEvent event) {
        Platform.runLater(this::clearData);
    }

    /**
     * Listens for a DisplayStarEvent and updates the star object on the UI thread.
     *
     * @param event The DisplayStarEvent to be handled.
     */
    @EventListener
    public void onDisplayStarEvent(DisplayStarEvent event) {
        Platform.runLater(() -> {
            log.info("STAR PROPERTIES PANE ::: Receive a display star event: star is:{}", event.getStarObject().getDisplayName());
            setStar(event.getStarObject());
        });
    }
}
