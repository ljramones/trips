package com.teamgannon.trips.dialogs.query;

import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.search.AstroSearchQuery;
import com.teamgannon.trips.search.SearchContext;
import com.teamgannon.trips.search.SearchPane;
import com.teamgannon.trips.utility.DialogUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;

import static com.teamgannon.trips.support.AlertFactory.showErrorAlert;


@Slf4j
public class QueryDialog extends Dialog<AstroSearchQuery> {

    public final Button runQueryButton = new Button("Run Query");
    public final Button dismissButton = new Button("Dismiss");
    private final @NotNull SearchPane searchPane;
    private final CheckBox plotDisplayCheckbox = new CheckBox("Plot Stars");
    private final CheckBox tableDisplayCheckbox = new CheckBox("Show Table");
    private final CheckBox exportCheckbox = new CheckBox("Export Selection to Excel");

    /**
     * constructor
     *
     * @param searchContext  the search context
     * @param eventPublisher the event publisher
     */
    public QueryDialog(@NotNull SearchContext searchContext,
                       ApplicationEventPublisher eventPublisher) {

        this.setTitle("Select stars from dataset");

        searchPane = new SearchPane(
                searchContext,
                eventPublisher);

        VBox vBox = new VBox();
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        Label explanationLabel = new Label("By default, this form returns all stars. You must click “Yes” on a category to have " +
                "its checkboxes limit your selection. After you click Yes, then only the stars with " +
                "the values you choose will be included in the selection.");
        hBox.getChildren().add(explanationLabel);
        hBox.setMinWidth(800);
        explanationLabel.setFont(Font.font("Verdana", FontWeight.BOLD, FontPosture.ITALIC, 10));

        vBox.getChildren().add(new Separator());
        vBox.getChildren().add(searchPane);
        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(10.0);

        HBox hBox1 = new HBox();
        hBox1.setAlignment(Pos.CENTER);
        hBox1.getChildren().add(plotDisplayCheckbox);
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 13);
        plotDisplayCheckbox.setFont(font);
        plotDisplayCheckbox.setSelected(true);
        hBox1.getChildren().add(new Separator());
        hBox1.getChildren().add(tableDisplayCheckbox);
        tableDisplayCheckbox.setFont(font);
        hBox1.getChildren().add(new Separator());
        exportCheckbox.setFont(font);
        hBox1.getChildren().add(exportCheckbox);
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
        DialogUtils.bindCloseHandler(this, this::close);
    }

    private void close(ActionEvent actionEvent) {
        setResult(new AstroSearchQuery());
        close();
    }


    public void setDataSetContext(@NotNull DataSetDescriptor descriptor) {
        searchPane.setDataSetContext(descriptor);
    }


    public void updateDataContext(@NotNull DataSetDescriptor dataSetDescriptor) {
        searchPane.updateDataContext(dataSetDescriptor);
    }

    public void refreshDataSets() {
        searchPane.refreshDataSets();
    }

    public void removeDataset(DataSetDescriptor dataSetDescriptor) {
        searchPane.removeDataset(dataSetDescriptor);
    }

    private void close(WindowEvent we) {
        setResult(new AstroSearchQuery());
        close();
    }

    private void runQueryClicked(ActionEvent actionEvent) {
        boolean showPlot = plotDisplayCheckbox.isSelected();
        boolean showTable = tableDisplayCheckbox.isSelected();
        boolean doExport = exportCheckbox.isSelected();

        if (!showPlot && !showTable && !doExport) {
            showErrorAlert("Query Request",
                    "Must select at least one target for data, plot, table, or export");
            return;
        }
        setResult(searchPane.runQuery(showPlot, showTable, doExport));
    }

}
