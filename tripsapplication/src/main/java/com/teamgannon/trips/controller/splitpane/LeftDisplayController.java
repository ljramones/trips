package com.teamgannon.trips.controller.splitpane;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.graphics.PlotManager;
import com.teamgannon.trips.graphics.panes.GalacticSpacePlane;
import com.teamgannon.trips.graphics.panes.InterstellarSpacePane;
import com.teamgannon.trips.graphics.panes.SolarSystemSpacePane;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class LeftDisplayController {

    @FXML
    private BorderPane leftBorderPane;

    @FXML
    private StackPane leftDisplayPane;

    private final GalacticSpacePlane galacticSpacePlane;
    private final SolarSystemSpacePane solarSystemSpacePane;
    private final InterstellarSpacePane interstellarSpacePane;
    private PlotManager plotManager;

    public LeftDisplayController(GalacticSpacePlane galacticSpacePlane,
                                 SolarSystemSpacePane solarSystemSpacePane,
                                 InterstellarSpacePane interstellarSpacePane) {
        this.galacticSpacePlane = galacticSpacePlane;
        this.solarSystemSpacePane = solarSystemSpacePane;
        this.interstellarSpacePane = interstellarSpacePane;
    }

    public void build(PlotManager plotManager) {
        this.plotManager = plotManager;
        leftBorderPane.setMinWidth(0);
        leftBorderPane.setPrefWidth(Universe.boxWidth * 0.6);

        leftDisplayPane.setMinWidth(Universe.boxWidth + 100);
        leftDisplayPane.setPrefWidth(Universe.boxWidth);

        leftBorderPane.setLeft(leftDisplayPane);

        createSolarSystemSpace();
        createInterstellarSpace();
        createGalacticSpace();
    }

    private void createSolarSystemSpace() {
        leftDisplayPane.getChildren().add(solarSystemSpacePane);
        solarSystemSpacePane.toBack();
    }

    private void createInterstellarSpace() {
        leftDisplayPane.getChildren().add(interstellarSpacePane);
        interstellarSpacePane.toFront();
        plotManager.setInterstellarPane(interstellarSpacePane);
    }

    private void createGalacticSpace() {
        leftDisplayPane.getChildren().add(galacticSpacePlane);
        galacticSpacePlane.toBack();
    }

    public void showInterstellar() {
        interstellarSpacePane.toFront();
    }

    public void showSolarSystem(StarDisplayRecord record) {
        solarSystemSpacePane.reset();
        solarSystemSpacePane.setSystemToDisplay(record);
        solarSystemSpacePane.toFront();
    }
}
