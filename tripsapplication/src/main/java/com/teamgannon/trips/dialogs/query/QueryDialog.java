package com.teamgannon.trips.dialogs.query;

import com.teamgannon.trips.config.application.DataSetContext;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.listener.DataSetChangeListener;
import com.teamgannon.trips.listener.StellarDataUpdaterListener;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.search.SearchPane;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;


@Slf4j
public class QueryDialog extends Dialog<AstroSearchQuery> {

    public final Button runQueryButton = new Button("Run Query");
    private final SearchContext searchContext;
    private DataSetContext dataSetContext;
    private final SearchPane searchPane;
    Button cancelDataSetButton = new Button("Dismiss");
    private final CheckBox plotDisplayCheckbox = new CheckBox("Show Plot");
    private final CheckBox tableDisplayCheckbox = new CheckBox("Show Table");

    /**
     * constructor
     *
     * @param searchContext  the search context
     * @param dataSetContext the data set context
     * @param updater        the data updater
     */
    public QueryDialog(SearchContext searchContext,
                       DataSetContext dataSetContext,
                       StellarDataUpdaterListener updater,
                       DataSetChangeListener dataSetChangeListener) {
        this.searchContext = searchContext;
        this.dataSetContext = dataSetContext;
        this.setTitle("Query And Search for Stars");

        searchPane = new SearchPane(
                this.searchContext,
                this.dataSetContext,
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
        hBox1.getChildren().add(new Separator());
        hBox1.getChildren().add(tableDisplayCheckbox);
        vBox.getChildren().add(hBox1);

        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox2);

        runQueryButton.setOnAction(this::runQueryclicked);
        hBox2.getChildren().add(runQueryButton);

        cancelDataSetButton.setOnAction(this::close);
        hBox2.getChildren().add(cancelDataSetButton);

        this.getDialogPane().setContent(vBox);

        // set the dialog as a utility
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);
    }


    public void setDataSetContext(DataSetDescriptor descriptor) {
        dataSetContext.setDataDescriptor(descriptor);
        searchPane.setDataSetContext(descriptor);
    }


    public void updateDataContext(DataSetDescriptor dataSetDescriptor) {
        searchPane.updateDataContext(dataSetDescriptor);
    }

    private void close(ActionEvent actionEvent) {
        setResult(searchContext.getAstroSearchQuery());
    }

    private void close(WindowEvent we) {
        setResult(searchContext.getAstroSearchQuery());
    }

    private void runQueryclicked(ActionEvent actionEvent) {
        boolean showPlot = plotDisplayCheckbox.isSelected();
        boolean showTable = tableDisplayCheckbox.isSelected();

        if (!showPlot && !showTable) {
            showErrorAlert("Query Request",
                    "Must select at leat one target for data, plot or table");
        }
        searchPane.runQuery(showPlot, showTable);
    }

}
