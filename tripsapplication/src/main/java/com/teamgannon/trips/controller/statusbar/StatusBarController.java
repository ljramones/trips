package com.teamgannon.trips.controller.statusbar;

import com.teamgannon.trips.algorithms.Universe;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StatusBarController {

    @Getter
    @FXML
    private HBox statusBar;

    @FXML
    private Label databaseCommentLabel;

    @FXML
    private Label databaseStatus;

    @FXML
    private Label routingStatusLabel;

    @FXML
    private Label routingStatus;

    @FXML
    public void initialize() {
        setupStatusbar();
    }

    private void setupStatusbar() {
        statusBar.setPrefWidth(Universe.boxWidth + 20);

        databaseCommentLabel.setTextFill(Color.BLACK);
        databaseStatus.setTextFill(Color.BLUE);
        routingStatusLabel.setTextFill(Color.BLACK);
        routingStatus.setTextFill(Color.SEAGREEN);
    }

    public void setStatus(String newStatus) {
        databaseStatus.setText(newStatus);
    }

    public void routingStatus(boolean statusFlag) {
        if (statusFlag) {
            routingStatus.setTextFill(Color.RED);
            routingStatus.setText("Active");
        } else {
            routingStatus.setTextFill(Color.SEAGREEN);
            routingStatus.setText("Inactive");
        }
    }
}
