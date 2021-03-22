package com.teamgannon.trips.dialogs.startup;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class FirstStartDialog extends Dialog<Boolean> {

    public FirstStartDialog() {
        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 15);

        VBox vBox = new VBox();
        Label title = new Label("Terran Interstellar Plotting System");
        title.setFont(font);
        vBox.getChildren().add(title);
        vBox.getChildren().add(new Separator());
        vBox.getChildren().add(new Label(
                """                                              
                        TRIPS doesn’t load any data by default, you will
                        need to import a dataset before you start.
                                                
                        A dataset derived from the Gaia 2 data is included as
                        a CSV for you to use, or you can provide your own.
                        
                        
                        """
        ));
        vBox.getChildren().add(new Separator());
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        Button dismissButton = new Button("Dismiss");
        dismissButton.setOnAction(this::dismiss);
        hBox.getChildren().add(dismissButton);

        this.getDialogPane().setContent(vBox);
    }

    private void dismiss(ActionEvent actionEvent) {
        setResult(true);
    }
}
