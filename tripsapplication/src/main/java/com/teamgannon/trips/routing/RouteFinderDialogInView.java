package com.teamgannon.trips.routing;

import com.teamgannon.trips.dialogs.search.ComboBoxAutoComplete;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
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
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;

@Slf4j
public class RouteFinderDialogInView extends Dialog<RouteFindingOptions> {

    /*
     * the combobox for selection
     */
    private final ComboBox<String> originDisplayCmb;
    private final ComboBox<String> destinationDisplayCmb;

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

    /**
     * this data set is used when we want to search the stars in the 3d display
     *
     * @param starsInView the set of visible stars on the display
     */
    public RouteFinderDialogInView(@NotNull List<StarDisplayRecord> starsInView) {
        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

        this.setTitle("Enter parameters for Route location");

        searchValues = convertList(starsInView);

        VBox vBox = new VBox();
        GridPane gridPane = new GridPane();

        Font font = topOfPanel(vBox, gridPane);

        originDisplayCmb = new ComboBox<>();
        originDisplayCmb.setPromptText("start typing");
        originDisplayCmb.setTooltip(new Tooltip());
        originDisplayCmb.getItems().addAll(searchValues);
        new ComboBoxAutoComplete<>(stage, originDisplayCmb);
        gridPane.add(originDisplayCmb, 1, 1);

        destinationDisplayCmb = new ComboBox<>();
        destinationDisplayCmb.setPromptText("start typing");
        destinationDisplayCmb.setTooltip(new Tooltip());
        destinationDisplayCmb.getItems().addAll(searchValues);
        new ComboBoxAutoComplete<>(stage, destinationDisplayCmb);
        gridPane.add(destinationDisplayCmb, 1, 2);

        setupRestOfPanel(vBox, gridPane, font);
    }

    private void close(WindowEvent windowEvent) {
        setResult(RouteFindingOptions.builder().selected(false).build());
    }

    ////////////////////////////////

    private Font topOfPanel(VBox vBox, GridPane gridPane) {
        this.setTitle("Enter parameters for Route location");

        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setVgap(5);
        gridPane.setHgap(5);
        vBox.getChildren().add(gridPane);

        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);

        Label originStar = new Label("Origin Star");
        originStar.setFont(font);
        gridPane.add(originStar, 0, 1);

        Label destinationStar = new Label("Destination Star");
        destinationStar.setFont(font);
        gridPane.add(destinationStar, 0, 2);
        return font;
    }

    private void setupRestOfPanel(VBox vBox, GridPane gridPane, Font font) {
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
        gridPane.add(routeColor, 0, 6);
        gridPane.add(colorPicker, 1, 6);
        colorPicker.setValue(Color.AQUA);

        Label numberPaths = new Label("number of paths to find");
        numberPaths.setFont(font);
        gridPane.add(numberPaths, 0, 7);
        gridPane.add(numPathsToFindTextField, 1, 7);
        numPathsToFindTextField.setText("3");

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
                            .originStar(originStarSelected)
                            .destinationStar(destinationStarSelected)
                            .upperBound(Double.parseDouble(upperLengthLengthTextField.getText()))
                            .lowerBound(Double.parseDouble(lowerLengthLengthTextField.getText()))
                            .lineWidth(Double.parseDouble(lineWidthTextField.getText()))
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
