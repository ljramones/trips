package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import com.teamgannon.trips.solarsysmodelling.accrete.StarSystem;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Optional;

import static com.teamgannon.trips.support.AlertFactory.showInfoMessage;

@Slf4j
public class PlanetDialog extends Dialog<Boolean> {

    private final StarSystem starSystem;
    private final SolarSystemReport solarSystemReport;

    public PlanetDialog(SolarSystemReport solarSystemReport) {
        this.solarSystemReport = solarSystemReport;
        this.starSystem = solarSystemReport.getStarSystem();

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
        HBox.setMargin(hBox, new Insets(0, 0, 0, 50));
        hBox.setSpacing(10);

        Button reportButton = new Button("Report");
        reportButton.setOnAction(this::showReport);
        hBox.getChildren().add(reportButton);


        Button cancelButton = new Button("Dismiss");
        cancelButton.setOnAction(this::close);
        hBox.getChildren().add(cancelButton);

        return hBox;
    }

    private void showReport(ActionEvent actionEvent) {
        SolarSystemReportDialog reportDialog = new SolarSystemReportDialog(solarSystemReport);
        Optional<SolarSystemReport> reportOptional = reportDialog.showAndWait();
        if (reportOptional.isPresent()) {
            SolarSystemReport saveReport = reportOptional.get();
            if (saveReport.isSaveSelected()) {
                // save file
                final FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save the generated solar system");
                File file = fileChooser.showSaveDialog(null);
                if (file != null) {
                    saveTextToFile(saveReport.getGeneratedReport(), file);
                } else {
                    log.warn("solar system generation save cancelled");
                    showInfoMessage("Solar System Generation", "Save cancelled");
                }
            }
        }
    }



    private void saveTextToFile(String generatedReport, File file) {
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(generatedReport);
        } catch (FileNotFoundException e) {
            log.error("Can't create file {} because of {}", file.getAbsolutePath(), e.getMessage());
        }
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
