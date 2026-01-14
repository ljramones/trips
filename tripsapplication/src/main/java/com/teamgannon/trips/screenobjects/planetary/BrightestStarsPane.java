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

    private Consumer<PlanetarySkyRenderer.BrightStarEntry> onStarSelected;

    public BrightestStarsPane() {
        setPadding(new Insets(10));
        setSpacing(5);

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

        getChildren().addAll(headerLabel, starListView);
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
