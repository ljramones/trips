package com.teamgannon.trips.report.route;

import com.teamgannon.trips.config.application.DataSetContext;
import com.teamgannon.trips.graphics.entities.RouteVisibility;
import com.teamgannon.trips.jpa.model.DataSetDescriptor;
import com.teamgannon.trips.routing.model.Route;
import com.teamgannon.trips.routing.tree.treemodel.RouteTree;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class RouteReportDialog extends Dialog<Boolean> {

    private final Map<String, DataSetDescriptor> lookUp = new HashMap<>();

    private final ChoiceBox<String> dataSetChoice;

    private Stage stage;

    public RouteReportDialog(DataSetContext dataSetContext, List<DataSetDescriptor> dataSetDescriptorList) {

        setTitle("Run Route Report for a Dataset");
        setWidth(200);
        VBox vBox = new VBox();
        vBox.setSpacing(10);
        vBox.setPadding(new Insets(10, 10, 10, 10));

        Set<String> keySet = buildLookup(dataSetDescriptorList);

        HBox hBox1 = new HBox();
        dataSetChoice = new ChoiceBox<>();
        dataSetChoice.setPrefWidth(175);
        dataSetChoice.getItems().addAll(keySet);
        dataSetChoice.setValue(dataSetContext.getDescriptor().getDataSetName());
        hBox1.setSpacing(10);
        hBox1.getChildren().addAll(new Label("Dataset to use:"), dataSetChoice);
        vBox.getChildren().add(hBox1);

        HBox hBox2 = new HBox();
        hBox2.setAlignment(Pos.CENTER);
        hBox2.setSpacing(5);
        vBox.getChildren().add(hBox2);

        Button dismissButton = new Button("Dismiss");
        dismissButton.setOnAction(this::dismissAction);

        Button runReportButton = new Button("Run Report");
        runReportButton.setOnAction(this::runReportAction);
        hBox2.getChildren().addAll(dismissButton, runReportButton);

        this.getDialogPane().setContent(vBox);

        stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.setOnCloseRequest(this::close);

    }

    private Set<String> buildLookup(List<DataSetDescriptor> dataSetDescriptorList) {
        dataSetDescriptorList.forEach(descriptor -> lookUp.put(descriptor.getDataSetName(), descriptor));
        return lookUp.keySet();
    }

    private void runReportAction(ActionEvent actionEvent) {
        String selected = dataSetChoice.getValue();
        DataSetDescriptor descriptor = lookUp.get(selected);
        String dataSetDescription = getDescription(descriptor);
        StringBuilder report = new StringBuilder(dataSetDescription);
        List<Route> routeList = descriptor.getRoutes();
        for (Route route : routeList) {
            RouteTree routeTree = RouteTree.createRouteTree(route, RouteVisibility.FULL);
            report.append(routeTree.getRoute());
        }

        RouteReportSaveDialog reportSaveDialog = new RouteReportSaveDialog(report.toString());
        reportSaveDialog.showAndWait();
        setResult(true);
    }

    private void saveTextToFile(String generatedReport, File file) {
        try (PrintWriter out = new PrintWriter(file)) {
            out.println(generatedReport);
        } catch (FileNotFoundException e) {
            log.error("Can't create file {} because of {}", file.getAbsolutePath(), e.getMessage());
        }
    }

    private String getDescription(DataSetDescriptor descriptor) {
        return descriptor.toString() + "\n\n";
    }

    private void close(WindowEvent windowEvent) {
        setResult(false);
    }

    private void dismissAction(ActionEvent actionEvent) {
        setResult(false);
    }

}
