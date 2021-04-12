package com.teamgannon.trips.dialogs.search;

import com.teamgannon.trips.dialogs.search.model.FindResults;
import com.teamgannon.trips.graphics.entities.StarDisplayRecord;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.textfield.TextFields;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Slf4j
public class FindStarInViewDialog extends Dialog<FindResults> {

    /**
     * the combobox for selection
     */
    private final @NotNull ComboBox<String> cmb;

    /**
     * our lookup
     */
    private final Map<String, StarDisplayRecord> starLookup = new HashMap<>();

    /**
     * the constructor
     *
     * @param starsInView the list of stars in view
     */
    public FindStarInViewDialog(@NotNull List<StarDisplayRecord> starsInView) {

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

        this.setTitle("Find a star in current view");
        this.setHeight(1000);
        this.setWidth(500);

        Set<String> searchValues = convertList(starsInView);

        VBox vBox = new VBox();
        Label whatToDoLabel = new Label("Please start typing the name of the star");
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        whatToDoLabel.setFont(font);
        vBox.getChildren().add(whatToDoLabel);
        vBox.getChildren().add(new Separator());

        HBox hBox = new HBox();

        cmb = new ComboBox<>();
        cmb.setPromptText("start typing");
        cmb.setTooltip(new Tooltip());
        cmb.getItems().addAll(searchValues);
        cmb.setEditable(true);
        TextFields.bindAutoCompletion(cmb.getEditor(), cmb.getItems());

        hBox.getChildren().add(new Label("Star to go to:     "));
        hBox.getChildren().add(cmb);
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);

        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox1);

        Button gotToStarButton = new Button("go to star");
        gotToStarButton.setOnAction(this::goToStarClicked);
        hBox1.getChildren().add(gotToStarButton);

        Button cancelDataSetButton = new Button("Cancel");
        cancelDataSetButton.setOnAction(this::close);
        hBox1.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);

    }

    private @NotNull Set<String> convertList(@NotNull List<StarDisplayRecord> starsInView) {
        starsInView.forEach(record -> starLookup.put(record.getStarName(), record));
        return starLookup.keySet();
    }

    private void close(WindowEvent windowEvent) {
        FindResults findResults = FindResults.builder().selected(false).build();
        setResult(findResults);
    }

    private void close(ActionEvent actionEvent) {
        FindResults findResults = FindResults.builder().selected(false).build();
        setResult(findResults);
    }

    private void goToStarClicked(ActionEvent actionEvent) {
        String value = cmb.getValue();
        if (value != null) {
            StarDisplayRecord record = starLookup.get(value);
            FindResults findResults = FindResults.builder().selected(true).record(record).build();
            setResult(findResults);
        }
    }


}
