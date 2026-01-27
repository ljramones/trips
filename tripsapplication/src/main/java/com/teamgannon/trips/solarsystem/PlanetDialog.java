package com.teamgannon.trips.solarsystem;

import com.teamgannon.trips.solarsysmodelling.accrete.Planet;
import com.teamgannon.trips.solarsysmodelling.accrete.SimStar;
import com.teamgannon.trips.solarsysmodelling.accrete.StarSystem;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
public class PlanetDialog extends Dialog<SolarSystemSaveResult> {

    private final StarSystem starSystem;
    private final SolarSystemReport solarSystemReport;

    public PlanetDialog(SolarSystemReport solarSystemReport) {
        this.solarSystemReport = solarSystemReport;
        this.starSystem = solarSystemReport.getStarSystem();

        this.setTitle("Details of Planetary System " + starSystem.getStarObject().getDisplayName());
        this.setResizable(true);

        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10));
        this.getDialogPane().setContent(vBox);
        this.getDialogPane().setPrefWidth(550);
        this.getDialogPane().setPrefHeight(550);

        // Add star info header with HZ boundaries
        GridPane starInfoPane = createStarInfoHeader(starSystem.getCentralBody());
        vBox.getChildren().add(starInfoPane);

        // Create tabbed pane with planet tabs
        TabPane planetaryTabPane = createPlanetaryTabPane(starSystem.getPlanets());

        // Wrap in ScrollPane for systems with many planets
        ScrollPane scrollPane = new ScrollPane(planetaryTabPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        vBox.getChildren().add(scrollPane);

        HBox buttonBox = createButtonBox();
        vBox.getChildren().add(buttonBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    private void close(WindowEvent windowEvent) {
        setResult(SolarSystemSaveResult.dismissed());
    }

    private void close(ActionEvent actionEvent) {
        setResult(SolarSystemSaveResult.dismissed());
    }

    private HBox createButtonBox() {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setPadding(new Insets(10, 0, 5, 0));
        hBox.setSpacing(15);

        Button saveButton = new Button("Save to Database");
        saveButton.setOnAction(this::saveToDatabase);
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        hBox.getChildren().add(saveButton);

        Button reportButton = new Button("Report");
        reportButton.setOnAction(this::showReport);
        hBox.getChildren().add(reportButton);

        Button cancelButton = new Button("Dismiss");
        cancelButton.setOnAction(this::close);
        hBox.getChildren().add(cancelButton);

        return hBox;
    }

    private GridPane createStarInfoHeader(SimStar star) {
        GridPane gridPane = new GridPane();
        gridPane.setHgap(15);
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(8, 12, 8, 12));
        gridPane.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-radius: 5;");

        // Star type and properties
        Label starTypeLabel = new Label("Star: " + star.toString().split(" HZ:")[0]);
        starTypeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        gridPane.add(starTypeLabel, 0, 0, 3, 1);

        // Habitable Zone boundaries with clarification
        Label hzHeaderLabel = new Label("Habitable Zone (liquid water possible):");
        hzHeaderLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666666; -fx-font-size: 10px;");
        gridPane.add(hzHeaderLabel, 0, 1, 3, 1);

        gridPane.add(new Label("Conservative:"), 0, 2);
        Label optimalHZLabel = new Label("%.2f - %.2f AU".formatted(
                star.getHzInnerOptimal(), star.getHzOuterOptimal()));
        optimalHZLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-weight: bold;");
        gridPane.add(optimalHZLabel, 1, 2);

        gridPane.add(new Label("Optimistic:"), 0, 3);
        Label maxHZLabel = new Label("%.2f - %.2f AU".formatted(
                star.getHzInnerMax(), star.getHzOuterMax()));
        maxHZLabel.setStyle("-fx-text-fill: #66BB6A;");
        gridPane.add(maxHZLabel, 1, 3);

        return gridPane;
    }

    private void saveToDatabase(ActionEvent actionEvent) {
        log.info("User requested to save generated solar system to database");
        SolarSystemSaveResult result = SolarSystemSaveResult.saveRequest(
                starSystem,
                solarSystemReport.getSourceStar()
        );
        setResult(result);
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
