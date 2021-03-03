package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import com.teamgannon.trips.solarsysmodelling.accrete.StarSystem;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.List;

public class PlanetDialog extends Dialog<Boolean> {

    private StarSystem starSystem;

    public PlanetDialog(StarSystem starSystem) {
        this.starSystem = starSystem;

        this.setTitle("Details of Planetary System " + starSystem.getStarObject().getDisplayName());
        this.setWidth(300);
        this.setHeight(400);

        VBox vBox = new VBox();
        this.getDialogPane().setContent(vBox);

        TabPane planetaryTabPane = createPlanetaryTabPane(starSystem.getPlanets());
        vBox.getChildren().add(planetaryTabPane);

        HBox buttonBox = createButtonBox();
        vBox.getChildren().add(buttonBox);


        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void close(WindowEvent windowEvent) {
        setResult(false);
    }

    private void close(ActionEvent actionEvent) {
        setResult(false);
    }

    private HBox createButtonBox() {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        Button cancelButton = new Button("Dismiss");
        cancelButton.setOnAction(this::close);
        hBox.getChildren().add(cancelButton);
        return hBox;
    }


    private TabPane createPlanetaryTabPane(List<Planet> planets) {
        TabPane tabPane = new TabPane();
        tabPane.setTabMaxWidth(400);
        int i = 1;
        for (Planet planet : planets) {
            Tab tab = new PlanetTab(planet, i++);
            tabPane.getTabs().add(tab);
        }
        return tabPane;
    }

}
