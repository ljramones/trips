package com.teamgannon.trips.routing.dialogs;

import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import com.teamgannon.trips.routing.dialogs.components.ColorChoice;
import com.teamgannon.trips.routing.dialogs.components.ColorChoiceDialog;
import com.teamgannon.trips.routing.model.RouteFindingOptions;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.textfield.TextFields;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class RouteFinderDialogInView extends Dialog<RouteFindingOptions> {

    /*
     * the combobox for selection
     */
    private ComboBox<String> originDisplayCmb;
    private ComboBox<String> destinationDisplayCmb;


    /**
     * our lookup
     */
    private final Map<String, StarDisplayRecord> starLookup = new HashMap<>();

    private final TextField upperLengthLengthTextField = new TextField();
    private final TextField lowerLengthLengthTextField = new TextField();

    private final TextField numPathsToFindTextField = new TextField();
    private final TextField lineWidthTextField = new TextField();

    private final ColorPicker colorPicker = new ColorPicker();

    private final Set<String> searchValues;

    // star types
    private final CheckBox oCheckBox = new CheckBox("O");
    private final CheckBox bCheckBox = new CheckBox("B");
    private final CheckBox aCheckBox = new CheckBox("A");
    private final CheckBox fCheckBox = new CheckBox("F");
    private final CheckBox gCheckBox = new CheckBox("G");
    private final CheckBox kCheckBox = new CheckBox("K");
    private final CheckBox mCheckBox = new CheckBox("M");
    private final CheckBox wCheckBox = new CheckBox("W");
    private final CheckBox lCheckBox = new CheckBox("L");
    private final CheckBox tCheckBox = new CheckBox("T");
    private final CheckBox yCheckBox = new CheckBox("Y");
    private final CheckBox cCheckBox = new CheckBox("C");
    private final CheckBox sCheckBox = new CheckBox("S");

    private final CheckBox terranCheckBox = new CheckBox("Terran");
    private final CheckBox dornaniCheckBox = new CheckBox("Dornani");
    private final CheckBox ktorCheckBox = new CheckBox("Ktor");
    private final CheckBox aratKurCheckBox = new CheckBox("Arat kur");
    private final CheckBox hkhRkhCheckBox = new CheckBox("Hkh'Rkh");
    private final CheckBox slassrithiCheckBox = new CheckBox("Slaasrithi");
    private final CheckBox other1CheckBox = new CheckBox("Other 1");
    private final CheckBox other2CheckBox = new CheckBox("Other 2");
    private final CheckBox other3CheckBox = new CheckBox("Other 3");
    private final CheckBox other4CheckBox = new CheckBox("Other 4");


    Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);


    /**
     * this data set is used when we want to search the stars in the 3d display
     *
     * @param starsInView the set of visible stars on the display
     */
    public RouteFinderDialogInView(@NotNull List<StarDisplayRecord> starsInView) {
        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

        searchValues = convertList(starsInView);

        this.setTitle("Enter parameters for Route location");

        Tab primaryTab = new Tab();
        setupPrimaryTab(primaryTab);
        TabPane routeSelectionPane = new TabPane();
        routeSelectionPane.getTabs().add(primaryTab);

        Tab starTab = new Tab();
        setupStarTab(starTab);
        routeSelectionPane.getTabs().add(starTab);

        Tab polityTab = new Tab();
        setupPolityTab(polityTab);
        routeSelectionPane.getTabs().add(polityTab);

        VBox vBox = new VBox();
        vBox.getChildren().add(routeSelectionPane);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);

        Button resetBtn = new Button("Find Route(s)");
        resetBtn.setOnAction(this::findRoutesClicked);
        hBox.getChildren().add(resetBtn);

        Button addBtn = new Button("Cancel");
        addBtn.setOnAction(this::cancelClicked);
        hBox.getChildren().add(addBtn);
        vBox.getChildren().add(hBox);

        this.getDialogPane().setContent(vBox);

    }

    private void setupPrimaryTab(Tab primaryTab) {
        VBox vBox = new VBox();
        primaryTab.setContent(vBox);
        primaryTab.setText("Primary");
        GridPane gridPane = new GridPane();

        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        vBox.getChildren().add(gridPane);

        Label originStar = new Label("Origin Star");
        originStar.setFont(font);
        gridPane.add(originStar, 0, 1);

        Label destinationStar = new Label("Destination Star");
        destinationStar.setFont(font);
        gridPane.add(destinationStar, 0, 2);

        originDisplayCmb = new ComboBox<>();
        originDisplayCmb.setPromptText("start typing");
        originDisplayCmb.setTooltip(new Tooltip());
        originDisplayCmb.getItems().addAll(searchValues);
        originDisplayCmb.setEditable(true);
        TextFields.bindAutoCompletion(originDisplayCmb.getEditor(), originDisplayCmb.getItems());
        gridPane.add(originDisplayCmb, 1, 1);

        destinationDisplayCmb = new ComboBox<>();
        destinationDisplayCmb.setPromptText("start typing");
        destinationDisplayCmb.setTooltip(new Tooltip());
        destinationDisplayCmb.getItems().addAll(searchValues);
        destinationDisplayCmb.setEditable(true);
        TextFields.bindAutoCompletion(destinationDisplayCmb.getEditor(), destinationDisplayCmb.getItems());
        gridPane.add(destinationDisplayCmb, 1, 2);

        Label upperBound = new Label("Upper limit for route length");
        upperBound.setFont(font);
        gridPane.add(upperBound, 0, 3);
        gridPane.add(upperLengthLengthTextField, 1, 3);
        upperLengthLengthTextField.setText("8");

        Label lowerBound = new Label("lower limit for route length");
        lowerBound.setFont(font);
        gridPane.add(lowerBound, 0, 4);
        gridPane.add(lowerLengthLengthTextField, 1, 4);
        lowerLengthLengthTextField.setText("3");

        Label lineWidth = new Label("route line width");
        lineWidth.setFont(font);
        gridPane.add(lineWidth, 0, 5);
        gridPane.add(lineWidthTextField, 1, 5);
        lineWidthTextField.setText("0.5");


        Label routeColor = new Label("route color");
        routeColor.setFont(font);


        Button colorButton = new Button("color");
        colorButton.setOnAction(this::pickColor);

        gridPane.add(routeColor, 0, 6);
        gridPane.add(colorButton, 1, 6);
        colorPicker.setValue(Color.AQUA);

        Label numberPaths = new Label("number of paths to find");
        numberPaths.setFont(font);
        gridPane.add(numberPaths, 0, 7);
        gridPane.add(numPathsToFindTextField, 1, 7);
        numPathsToFindTextField.setText("3");

    }

    private void pickColor(ActionEvent actionEvent) {
        ColorChoiceDialog dialog = new ColorChoiceDialog();
        Optional<ColorChoice> colorChoiceOptional = dialog.showAndWait();
        if (colorChoiceOptional.isPresent()) {
            ColorChoice colorChoice = colorChoiceOptional.get();
            if (colorChoice.isSelected()) {
                colorPicker.setValue(colorChoice.getSwatch());
            }
        }
    }

    private void setupStarTab(Tab starTab) {
        VBox vBox = new VBox();
        starTab.setContent(vBox);
        starTab.setText("Star Exclusions");
        Label titleLabel = new Label("Select stars to exclude in our route finding");
        titleLabel.setFont(font);
        vBox.getChildren().add(titleLabel);
        vBox.getChildren().add(new Separator());

        HBox hBox = new HBox();
        vBox.getChildren().add(hBox);

        VBox vBox1 = new VBox();
        hBox.getChildren().add(vBox1);
        oCheckBox.setMinWidth(100);
        vBox1.getChildren().add(oCheckBox);
        bCheckBox.setMinWidth(100);
        vBox1.getChildren().add(bCheckBox);
        aCheckBox.setMinWidth(100);
        vBox1.getChildren().add(aCheckBox);
        fCheckBox.setMinWidth(100);
        vBox1.getChildren().add(fCheckBox);

        VBox vBox2 = new VBox();
        hBox.getChildren().add(vBox2);
        gCheckBox.setMinWidth(100);
        vBox2.getChildren().add(gCheckBox);
        kCheckBox.setMinWidth(100);
        vBox2.getChildren().add(kCheckBox);
        mCheckBox.setMinWidth(100);
        vBox2.getChildren().add(mCheckBox);
        wCheckBox.setMinWidth(100);
        vBox2.getChildren().add(wCheckBox);

        VBox vBox3 = new VBox();
        hBox.getChildren().add(vBox3);
        lCheckBox.setMinWidth(100);
        vBox3.getChildren().add(lCheckBox);
        tCheckBox.setMinWidth(100);
        vBox3.getChildren().add(tCheckBox);
        yCheckBox.setMinWidth(100);
        vBox3.getChildren().add(yCheckBox);
        cCheckBox.setMinWidth(100);
        vBox3.getChildren().add(cCheckBox);
        sCheckBox.setMinWidth(100);
        vBox3.getChildren().add(sCheckBox);

    }

    private void setupPolityTab(Tab polityTab) {
        VBox vBox = new VBox();
        polityTab.setContent(vBox);
        polityTab.setText("Polity Exclusions");
        Label titleLabel = new Label("Select polities to exclude in our route finding");
        titleLabel.setFont(font);
        vBox.getChildren().add(titleLabel);
        vBox.getChildren().add(new Separator());

        HBox hBox = new HBox();
        vBox.getChildren().add(hBox);

        VBox vBox1 = new VBox();
        hBox.getChildren().add(vBox1);
        terranCheckBox.setMinWidth(100);
        vBox1.getChildren().add(terranCheckBox);

        dornaniCheckBox.setMinWidth(100);
        vBox1.getChildren().add(dornaniCheckBox);

        ktorCheckBox.setMinWidth(100);
        vBox1.getChildren().add(ktorCheckBox);

        aratKurCheckBox.setMinWidth(100);
        vBox1.getChildren().add(aratKurCheckBox);

        hkhRkhCheckBox.setMinWidth(100);
        vBox1.getChildren().add(hkhRkhCheckBox);

        slassrithiCheckBox.setMinWidth(100);
        vBox1.getChildren().add(slassrithiCheckBox);

        VBox vBox2 = new VBox();
        hBox.getChildren().add(vBox2);

        other1CheckBox.setMinWidth(100);
        vBox2.getChildren().add(other1CheckBox);

        other2CheckBox.setMinWidth(100);
        vBox2.getChildren().add(other2CheckBox);

        other3CheckBox.setMinWidth(100);
        vBox2.getChildren().add(other3CheckBox);

        other4CheckBox.setMinWidth(100);
        vBox2.getChildren().add(other4CheckBox);

    }

    private Set<String> getStarExclusions() {
        Set<String> starExclusions = new HashSet<>();
        if (oCheckBox.isSelected()) {
            starExclusions.add("O");
        }
        if (bCheckBox.isSelected()) {
            starExclusions.add("B");
        }
        if (aCheckBox.isSelected()) {
            starExclusions.add("A");
        }
        if (fCheckBox.isSelected()) {
            starExclusions.add("F");
        }
        if (gCheckBox.isSelected()) {
            starExclusions.add("G");
        }
        if (kCheckBox.isSelected()) {
            starExclusions.add("K");
        }
        if (mCheckBox.isSelected()) {
            starExclusions.add("M");
        }
        if (wCheckBox.isSelected()) {
            starExclusions.add("W");
        }
        if (lCheckBox.isSelected()) {
            starExclusions.add("L");
        }
        if (tCheckBox.isSelected()) {
            starExclusions.add("T");
        }
        if (yCheckBox.isSelected()) {
            starExclusions.add("Y");
        }
        if (cCheckBox.isSelected()) {
            starExclusions.add("C");
        }
        if (sCheckBox.isSelected()) {
            starExclusions.add("S");
        }

        return starExclusions;
    }

    private Set<String> getPolityExclusions() {
        Set<String> exclusions = new HashSet<>();
        if (terranCheckBox.isSelected()) {
            exclusions.add("Terran");
        }
        if (dornaniCheckBox.isSelected()) {
            exclusions.add("Dornani");
        }
        if (ktorCheckBox.isSelected()) {
            exclusions.add("Ktor");
        }
        if (aratKurCheckBox.isSelected()) {
            exclusions.add("Arat Kur");
        }
        if (hkhRkhCheckBox.isSelected()) {
            exclusions.add("Hkh'rkh");
        }
        if (slassrithiCheckBox.isSelected()) {
            exclusions.add("slassrithi");
        }
        if (other1CheckBox.isSelected()) {
            exclusions.add("Other 1");
        }
        if (other2CheckBox.isSelected()) {
            exclusions.add("Other 2");
        }
        if (other3CheckBox.isSelected()) {
            exclusions.add("Other 3");
        }
        if (other4CheckBox.isSelected()) {
            exclusions.add("Other4");
        }

        return exclusions;
    }


    private void close(WindowEvent windowEvent) {
        setResult(RouteFindingOptions.builder().selected(false).build());
    }

    ////////////////////////////////

    private @NotNull Set<String> convertList(@NotNull List<StarDisplayRecord> starsInView) {
        for (StarDisplayRecord record : starsInView) {
            starLookup.put(record.getStarName(), record);
        }
        return starLookup.keySet();
    }

    private void cancelClicked(ActionEvent actionEvent) {
        setResult(RouteFindingOptions.builder().selected(false).build());
        log.info("cancel find routes clicked");
    }

    private void findRoutesClicked(ActionEvent actionEvent) {
        try {
            String originStarSelected = originDisplayCmb.getValue();
            String destinationStarSelected = destinationDisplayCmb.getValue();
            double maxDistance = 20;

            if (!searchValues.contains(originStarSelected)) {
                showErrorAlert("Find Route", String.format("Origin star <%s> is not present in view", originStarSelected));
                return;
            }
            if (!searchValues.contains(destinationStarSelected)) {
                showErrorAlert("Find Route", String.format("Destination star <%s> is not present in view", destinationStarSelected));
                return;
            }

            setResult(
                    RouteFindingOptions
                            .builder()
                            .selected(true)
                            .originStarName(originStarSelected)
                            .destinationStarName(destinationStarSelected)
                            .upperBound(Double.parseDouble(upperLengthLengthTextField.getText()))
                            .lowerBound(Double.parseDouble(lowerLengthLengthTextField.getText()))
                            .lineWidth(Double.parseDouble(lineWidthTextField.getText()))
                            .starExclusions(getStarExclusions())
                            .polityExclusions(getPolityExclusions())
                            .color(colorPicker.getValue())
                            .maxDistance(maxDistance)
                            .numberPaths(Integer.parseInt(numPathsToFindTextField.getText()))
                            .build()
            );
            log.info("cancel clicked");
        } catch (NumberFormatException nfe) {
            showErrorAlert("Route Finder", "bad floating point");
            log.error("bad floating point");
        }
    }

}
