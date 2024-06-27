package com.teamgannon.trips.controller.menubar;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FileMenuController {
    @FXML
    public MenuItem importDataSetMenuItem;
    @FXML
    public MenuItem openDatasetMenuItem;
    @FXML
    public MenuItem saveMenuItem;
    @FXML
    public MenuItem quitMenuItem;

    public void loadDataSetManager(ActionEvent actionEvent) {

    }

    public void selectActiveDataset(ActionEvent actionEvent) {

    }

    public void saveDataset(ActionEvent actionEvent) {

    }

    public void quit(ActionEvent actionEvent) {

    }
}
