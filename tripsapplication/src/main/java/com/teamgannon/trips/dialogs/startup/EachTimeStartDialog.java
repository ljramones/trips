package com.teamgannon.trips.dialogs.startup;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public class EachTimeStartDialog extends Dialog<Boolean> {

    private final CheckBox onStartup = new CheckBox("Don’t show this at startup?");

    public EachTimeStartDialog() {

        Font font = Font.font("Verdana", FontWeight.BOLD, FontPosture.REGULAR, 15);

        VBox vBox = new VBox();
        Label title = new Label("Terran Interstellar Plotting System");
        title.setFont(font);
        vBox.getChildren().add(title);
        vBox.getChildren().add(new Separator());
        vBox.getChildren().add(new Label(
                """                       
                        When the program starts
                        Please import a dataset
                            -->  Star Data/Import data set from a file....
                        Then
                            -->  Star Data/Select Active Dataset...    
                                               
                        """
        ));
        vBox.getChildren().add(new Separator());
        vBox.getChildren().add(onStartup);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        Button dismissButton = new Button("Dismiss");
        dismissButton.setOnAction(this::dismiss);
        hBox.getChildren().add(dismissButton);

        this.getDialogPane().setContent(vBox);
    }

    private void dismiss(ActionEvent actionEvent) {
        setResult(onStartup.isSelected());
    }

}
