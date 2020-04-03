package com.teamgannon.trips.graphics.panes;

import com.teamgannon.trips.algorithms.Universe;
import com.teamgannon.trips.graphics.operators.ContextSelector;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;

/**
 * This is used to display a solar system
 * <p>
 * Created by larrymitchell on 2017-02-05.
 */
public class SolarSystemSpacePane extends Pane {

    private Label systemIdentifierLabel;

    private String systemName = "No System Selected";
    private Button returnButton;

    /**
     * the universe model which holds detail about our pocket universe
     */
    private Universe universe = new Universe();
    private Map<String, String> objectProperties;
    private ContextSelector contextSelector;


    public SolarSystemSpacePane(double width, double height) {
        setStyle("-fx-background-color: black");

        // setup the title panes
        GridPane titlePane = setupGridPane();

        // add title panes to display
        this.getChildren().add(titlePane);
    }

    /**
     * set the system to show
     *
     * @param objectProperties object properties of this system
     */
    public void setSystemToDisplay(Map<String, String> objectProperties) {
        this.objectProperties = objectProperties;
        systemName = objectProperties.get("name");
        systemIdentifierLabel.setText(systemName);
    }

    /**
     * used to draw the target System
     */
    public void render() {

    }

    // ---------------------- helpers -------------------------- //

    private GridPane setupGridPane() {
        // setup system label to show name of system you are in
        systemIdentifierLabel = new Label(systemName);
        systemIdentifierLabel.setFont(Font.font("Cambria", 20));
        systemIdentifierLabel.setTextFill(Color.WHITE);

        // setup return button to jump back to interstellar space
        returnButton = new Button("Jump Back");
        returnButton.setOnAction(e -> jumpBackToInterstellarSpace());

        GridPane titlePane = new GridPane();
//        titlePane.setStyle("-fx-background-color: white");
        Separator separator1 = new Separator();
        separator1.setMinWidth(40.);
        titlePane.addRow(0, returnButton, separator1, systemIdentifierLabel);
        return titlePane;
    }


    private void jumpBackToInterstellarSpace() {
        // there is no specific context at the moment.  We assume the same interstellar space we came form
        contextSelector.selectInterstellarSpace(new HashMap<>());
    }


    public void setContextUpdater(ContextSelector contextSelector) {
        this.contextSelector = contextSelector;
    }
}
