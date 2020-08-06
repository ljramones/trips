package com.teamgannon.trips.dialogs;

import com.teamgannon.trips.dialogs.support.EditTypeEnum;
import com.teamgannon.trips.dialogs.support.TableEditResult;
import com.teamgannon.trips.tableviews.StarEditRecord;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class AddStarDialog extends Dialog<TableEditResult> {

    private StarEditRecord starEditRecord = new StarEditRecord();

    public Button addStarButton = new Button("Add Star");

    private final TextField starName = new TextField();
    private final TextField distanceToEarth = new TextField();
    private final TextField spectra = new TextField();
    private final TextField radius = new TextField();
    private final TextField ra = new TextField();
    private final TextField declination = new TextField();
    private final TextField parallax = new TextField();
    private final TextField luminosity = new TextField();
    private final TextField xCoord = new TextField();
    private final TextField yCoord = new TextField();
    private final TextField zCoord = new TextField();
    private final CheckBox real = new CheckBox("Real/Fictional");

    private final TextArea notes = new TextArea();


    public AddStarDialog(StarEditRecord starEditRecord) {
        this();
        this.setTitle("Update a star dialog");
        addStarButton.setText("Update Star");
        this.starEditRecord = starEditRecord;
        setData();
    }

    public AddStarDialog() {
        this.setHeight(300);
        this.setWidth(400);

        VBox vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);
        this.getDialogPane().setContent(vBox);
        this.setTitle("Add a star dialog");

        GridPane gridPane = new GridPane();
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        vBox.getChildren().add(gridPane);

        Label starNameLabel = new Label("Star Name:");
        gridPane.add(starNameLabel, 0, 0);
        gridPane.add(starName, 1, 0);

        Label distanceToEarthLabel = new Label("Distance to Earth (ly):");
        gridPane.add(distanceToEarthLabel, 0, 1);
        gridPane.add(distanceToEarth, 1, 1);

        Label spectraLabel = new Label("Spectra:");
        gridPane.add(spectraLabel, 0, 2);
        gridPane.add(spectra, 1, 2);

        Label radiusLabel = new Label("Radius:");
        gridPane.add(radiusLabel, 0, 3);
        gridPane.add(radius, 1, 3);

        Label raLabel = new Label("Right Ascension:");
        gridPane.add(raLabel, 0, 4);
        gridPane.add(ra, 1, 4);

        Label decLabel = new Label("Declination:");
        gridPane.add(decLabel, 0, 5);
        gridPane.add(declination, 1, 5);

        Label paraLabel = new Label("Parallax:");
        gridPane.add(paraLabel, 0, 6);
        gridPane.add(parallax, 1, 6);

        Label lumLabel = new Label("Luminosity:");
        gridPane.add(lumLabel, 0, 7);
        gridPane.add(luminosity, 1, 7);

        Label xLabel = new Label("X:");
        gridPane.add(xLabel, 0, 8);
        gridPane.add(xCoord, 1, 8);

        Label yLabel = new Label("Y:");
        gridPane.add(yLabel, 0, 9);
        gridPane.add(yCoord, 1, 9);

        Label zLabel = new Label("Z:");
        gridPane.add(zLabel, 0, 10);
        gridPane.add(zCoord, 1, 10);

        gridPane.add(real, 0, 11, 2, 1);

        Label notesLabel = new Label("Notes:");
        gridPane.add(notesLabel, 0, 12);
        gridPane.add(notes, 1, 12, 1, 4);

        HBox hBox5 = new HBox();
        hBox5.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox5);

        addStarButton.setText("Add Star");
        addStarButton.setOnAction(this::addDataSetClicked);
        hBox5.getChildren().add(addStarButton);

        Button cancelDataSetButton = new Button("Cancel");
        cancelDataSetButton.setOnAction(this::cancel);
        hBox5.getChildren().add(cancelDataSetButton);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }

    /**
     * close the dialog from the close button
     *
     * @param actionEvent the action event
     */
    private void cancel(ActionEvent actionEvent) {
        setResult(new TableEditResult(EditTypeEnum.CANCEL, null));
    }

    /**
     * close the dialog from stage x button
     *
     * @param we the windows event
     */
    private void close(WindowEvent we) {
        setResult(new TableEditResult(EditTypeEnum.CANCEL, null));
    }

    private void addDataSetClicked(ActionEvent actionEvent) {
        // pull the data from the controls
        getData();

        // do a validity check for each iem
        if (starEditRecord.getDisplayName().isEmpty()) {
            showErrorAlert("Add Star", "Star name cannot be empty!");
        }

        if (starEditRecord.getSpectra().isEmpty()) {
            showErrorAlert("Add Star", "Spectra cannot be empty!");
        }

        if (starEditRecord.getComment().isEmpty()) {
            showErrorAlert("Add Star", "Notes field cannot be empty!");
        }

        setResult(new TableEditResult(EditTypeEnum.UPDATE, starEditRecord));
    }

    private void getData() {
        starEditRecord.setDisplayName(starName.getText());

        if (!NumberUtils.isCreatable(distanceToEarth.getText())) {
            showErrorAlert("Add Star", "Distance to earth is not a valid double/float!");
        }
        starEditRecord.setDistanceToEarth(Double.parseDouble(distanceToEarth.getText()));
        starEditRecord.setSpectra(spectra.getText());

        if (!NumberUtils.isCreatable(radius.getText())) {
            showErrorAlert("Add Star", "Radius is not a valid double/float!");
        }
        starEditRecord.setRadius(Double.parseDouble(radius.getText()));

        if (!NumberUtils.isCreatable(ra.getText())) {
            showErrorAlert("Add Star", "RA is not a valid double/float!");
        }
        starEditRecord.setRa(Double.parseDouble(ra.getText()));

        if (!NumberUtils.isCreatable(declination.getText())) {
            showErrorAlert("Add Star", "Declination is not a valid double/float!");
        }
        starEditRecord.setDeclination(Double.parseDouble(declination.getText()));

        if (!parallax.getText().isEmpty()) {
            starEditRecord.setParallax(Double.parseDouble(parallax.getText()));
        }

        if (!NumberUtils.isCreatable(xCoord.getText())) {
            showErrorAlert("Add Star", "X is not a valid double/float!");
        }
        starEditRecord.setXCoord(Double.parseDouble(xCoord.getText()));

        if (!NumberUtils.isCreatable(yCoord.getText())) {
            showErrorAlert("Add Star", "Y is not a valid double/float!");
        }
        starEditRecord.setYCoord(Double.parseDouble(yCoord.getText()));

        if (!NumberUtils.isCreatable(zCoord.getText())) {
            showErrorAlert("Add Star", "Z is not a valid double/float!");
        }
        starEditRecord.setZCoord(Double.parseDouble(zCoord.getText()));
        starEditRecord.setReal(real.isSelected());
        starEditRecord.setComment(notes.getText());
    }


    private void setData() {
        starName.setText(starEditRecord.getDisplayName());
        if (starEditRecord.getDistanceToEarth() != null) {
            distanceToEarth.setText(starEditRecord.getDistanceToEarth().toString());
        }
        spectra.setText(starEditRecord.getSpectra());
        if (starEditRecord.getRadius() != null) {
            radius.setText(starEditRecord.getRadius().toString());
        }
        ra.setText(starEditRecord.getRa().toString());
        if (starEditRecord.getDeclination() != null) {
            declination.setText(starEditRecord.getDeclination().toString());
        }
        if (starEditRecord.getParallax() != null) {
            parallax.setText(starEditRecord.getParallax().toString());
        }
        luminosity.setText(starEditRecord.getDisplayName());
        if (starEditRecord.getXCoord() != null) {
            xCoord.setText(starEditRecord.getXCoord().toString());
        }
        if (starEditRecord.getYCoord() != null) {
            yCoord.setText(starEditRecord.getYCoord().toString());
        }
        if (starEditRecord.getZCoord() != null) {
            zCoord.setText(starEditRecord.getZCoord().toString());
        }
        real.setSelected(starEditRecord.isReal());
        notes.setText(starEditRecord.getComment());
    }

}
