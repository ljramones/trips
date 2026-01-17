package com.teamgannon.trips.screenobjects.planetary;

import com.teamgannon.trips.planetary.rendering.PlanetarySkyRenderer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Shows the top 20 brightest stars visible from the planet's surface.
 * Each entry shows name, distance, apparent magnitude, and direction.
 */
@Slf4j
@Component
public class BrightestStarsPane extends VBox {

    private final ListView<PlanetarySkyRenderer.BrightStarEntry> starListView;
    private final ObservableList<PlanetarySkyRenderer.BrightStarEntry> starList;
    private final Label headerLabel;
    private final VBox selectedStarBox;
    private final Label selectedStarLabel;
    private final Label selectedStarDetails;

    private Consumer<PlanetarySkyRenderer.BrightStarEntry> onStarSelected;

    public BrightestStarsPane() {
        setPadding(new Insets(10));
        setSpacing(5);

        // Selected star section (shown when a star is clicked in the sky)
        selectedStarBox = new VBox(3);
        selectedStarBox.setPadding(new Insets(5, 8, 8, 8));
        selectedStarBox.setStyle("-fx-background-color: #2a4d6e; -fx-background-radius: 4;");
        selectedStarBox.setVisible(false);
        selectedStarBox.setManaged(false);

        Label selectedHeader = new Label("Selected Star");
        selectedHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #aaccff;");
        selectedStarLabel = new Label();
        selectedStarLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: white;");
        selectedStarDetails = new Label();
        selectedStarDetails.setStyle("-fx-font-size: 11px; -fx-text-fill: #cccccc;");
        selectedStarDetails.setWrapText(true);
        selectedStarBox.getChildren().addAll(selectedHeader, selectedStarLabel, selectedStarDetails);

        headerLabel = new Label("Top 20 Brightest Stars");
        headerLabel.setStyle("-fx-font-weight: bold;");

        starList = FXCollections.observableArrayList();
        starListView = new ListView<>(starList);
        starListView.setPrefHeight(250);

        // Custom cell factory
        starListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PlanetarySkyRenderer.BrightStarEntry entry, boolean empty) {
                super.updateItem(entry, empty);
                if (empty || entry == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String compassDir = getCompassDirection(entry.getAzimuth());
                    setText(String.format("%s  mag:%.1f  %.1f ly  %s %.0f°",
                            entry.getName(),
                            entry.getApparentMagnitude(),
                            entry.getDistanceFromPlanet(),
                            compassDir,
                            entry.getAltitude()));
                }
            }
        });

        // Selection listener
        starListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && onStarSelected != null) {
                onStarSelected.accept(newVal);
            }
        });

        getChildren().addAll(selectedStarBox, headerLabel, starListView);
    }

    /**
     * Display a star that was clicked in the sky.
     * Shows detailed info in the selected star box.
     */
    public void showSelectedStar(PlanetarySkyRenderer.BrightStarEntry star) {
        if (star == null) {
            selectedStarBox.setVisible(false);
            selectedStarBox.setManaged(false);
            return;
        }

        selectedStarLabel.setText(star.getName());

        String compassDir = getCompassDirection(star.getAzimuth());
        String details = String.format(
                "Magnitude: %.2f\n" +
                "Distance: %.2f ly\n" +
                "Position: %s, %.1f° altitude\n" +
                "Azimuth: %.1f°",
                star.getApparentMagnitude(),
                star.getDistanceFromPlanet(),
                compassDir,
                star.getAltitude(),
                star.getAzimuth()
        );

        // Add spectral class if available
        if (star.getStarRecord() != null && star.getStarRecord().getSpectralClass() != null) {
            details += "\nSpectral: " + star.getStarRecord().getSpectralClass();
        }

        selectedStarDetails.setText(details);
        selectedStarBox.setVisible(true);
        selectedStarBox.setManaged(true);

        // Also try to select it in the list if it's there
        for (int i = 0; i < starList.size(); i++) {
            if (starList.get(i).getName().equals(star.getName())) {
                starListView.getSelectionModel().select(i);
                starListView.scrollTo(i);
                break;
            }
        }

        log.info("Selected star: {}", star.getName());
    }

    /**
     * Clear the selected star display.
     */
    public void clearSelectedStar() {
        selectedStarBox.setVisible(false);
        selectedStarBox.setManaged(false);
        starListView.getSelectionModel().clearSelection();
    }

    /**
     * Set the list of brightest stars to display.
     */
    public void setStars(List<PlanetarySkyRenderer.BrightStarEntry> stars) {
        starList.clear();
        if (stars != null) {
            starList.addAll(stars);
        }

        headerLabel.setText(String.format("Top %d Brightest Stars", starList.size()));
        log.debug("Updated brightest stars list with {} entries", starList.size());
    }

    /**
     * Clear the star list.
     */
    public void clear() {
        starList.clear();
        headerLabel.setText("Top 20 Brightest Stars");
        clearSelectedStar();
    }

    /**
     * Set callback for when a star is selected.
     */
    public void setOnStarSelected(Consumer<PlanetarySkyRenderer.BrightStarEntry> callback) {
        this.onStarSelected = callback;
    }

    /**
     * Get the selected star.
     */
    public PlanetarySkyRenderer.BrightStarEntry getSelectedStar() {
        return starListView.getSelectionModel().getSelectedItem();
    }

    /**
     * Convert azimuth to compass direction.
     */
    private String getCompassDirection(double azimuth) {
        azimuth = ((azimuth % 360) + 360) % 360;

        if (azimuth < 22.5 || azimuth >= 337.5) return "N";
        if (azimuth < 67.5) return "NE";
        if (azimuth < 112.5) return "E";
        if (azimuth < 157.5) return "SE";
        if (azimuth < 202.5) return "S";
        if (azimuth < 247.5) return "SW";
        if (azimuth < 292.5) return "W";
        return "NW";
    }
}
