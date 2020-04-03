package com.teamgannon.trips.controllers;


import com.teamgannon.trips.javafxspringbootsupport.AbstractFxmlView;
import com.teamgannon.trips.javafxspringbootsupport.FXMLView;

@FXMLView(
        value = "/fxml/maincontroller.fxml",
        css = {"/fxml/styles.css"}
)
public class MainViewer extends AbstractFxmlView {

    public MainViewer() {
        setTitle("Terran Republic Interstellar Plotter");
    }

}
