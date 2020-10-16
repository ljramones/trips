package com.teamgannon.trips.dialogs.query;

import com.teamgannon.trips.config.application.DataSetContext;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StellarDataUpdaterListener;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.search.SearchPane;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;


@Slf4j
public class QueryDialog extends Dialog<AstroSearchQuery> {

    public final Button runQueryButton = new Button("Run Query");
    public final Button dismissButton = new Button("Dismiss");
    private final SearchPane searchPane;
    private final CheckBox plotDisplayCheckbox = new CheckBox("Plot Stars");
    private final CheckBox tableDisplayCheckbox = new CheckBox("Show Table");


    private static double xOffset = 0;
    private static double yOffset = 0;


    /**
     * constructor
     *
     * @param searchContext  the search context
     * @param dataSetContext the data set context
     * @param updater        the data updater
     */
    public QueryDialog(Stage stage,
                       SearchContext searchContext,
                       DataSetContext dataSetContext,
                       StellarDataUpdaterListener updater,
                       DataSetChangeListener dataSetChangeListener) {
        this.setTitle("Query and Search");

        searchPane = new SearchPane(
                stage,
                searchContext,
                dataSetContext,
                dataSetChangeListener,
                updater);

        this.setHeight(1000);
        this.setWidth(500);

        VBox vBox = new VBox();

        vBox.getChildren().add(searchPane);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);

        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        hBox1.getChildren().add(plotDisplayCheckbox);
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        plotDisplayCheckbox.setFont(font);
        hBox1.getChildren().add(new Separator());
        hBox1.getChildren().add(tableDisplayCheckbox);
        tableDisplayCheckbox.setFont(font);
        vBox.getChildren().add(hBox1);

        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox2);

        runQueryButton.setOnAction(this::runQueryClicked);
        hBox2.getChildren().add(runQueryButton);

        dismissButton.setOnAction(this::close);
        hBox2.getChildren().add(dismissButton);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        stage.setOnCloseRequest(this::close);
    }

    private void close(ActionEvent actionEvent) {
        setResult(new AstroSearchQuery());
    }


    public void setDataSetContext(DataSetDescriptor descriptor) {
        searchPane.setDataSetContext(descriptor);
    }


    public void updateDataContext(DataSetDescriptor dataSetDescriptor) {
        searchPane.updateDataContext(dataSetDescriptor);
    }

    private void close(WindowEvent we) {
        setResult(new AstroSearchQuery());
    }

    private void runQueryClicked(ActionEvent actionEvent) {
        boolean showPlot = plotDisplayCheckbox.isSelected();
        boolean showTable = tableDisplayCheckbox.isSelected();

        if (!showPlot && !showTable) {
            showErrorAlert("Query Request",
                    "Must select at least one target for data, plot or table");
        }
        searchPane.runQuery(showPlot, showTable);
    }

}
