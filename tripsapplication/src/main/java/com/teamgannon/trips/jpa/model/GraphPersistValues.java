package com.teamgannon.trips.jpa.model;

import com.teamgannon.trips.config.application.model.ColorPalette;
import com.teamgannon.trips.dialogs.preferencespanes.model.GraphEnables;
import javafx.scene.paint.Color;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
public class GraphPersistValues implements Serializable {

    @Id
    private String id;

    /**
     * label color on graph
     */
    private String labelColor;

    /**
     * the grid color on the graph
     */
    private String gridColor;

    /**
     * the stem color on the graph
     */
    private String extensionColor;

    /**
     * the legend color on the graph
     */
    private String legendColor;

    /**
     * the display grid
     */
    private boolean displayGrid = true;

    /**
     * the display stems
     */
    private boolean displayStems = true;

    /**
     * the display labels
     */
    private boolean displayLabels = true;

    /**
     * the display legends
     */
    private boolean displayLegend = true;


    /**
     * initialize the colors with a default
     */
    public void init() {
        id = UUID.randomUUID().toString();
        labelColor = Color.BEIGE.toString();
        gridColor = Color.MEDIUMBLUE.toString();
        extensionColor = Color.DARKSLATEBLUE.toString();
        legendColor = Color.BEIGE.toString();
        displayGrid = true;
        displayStems = true;
        displayLabels = true;
        displayLegend = true;
    }

    /**
     * get the default colors
     *
     * @return a data structure with default values
     */
    public static GraphPersistValues defaults() {
        GraphPersistValues graphPersistValues = new GraphPersistValues();
        graphPersistValues.init();
        return graphPersistValues;
    }

    public void setColors(ColorPalette graphColors) {
        gridColor = graphColors.getGridColor().toString();
        extensionColor = graphColors.getExtensionColor().toString();
        labelColor = graphColors.getLabelColor().toString();
        legendColor = graphColors.getLegendColor().toString();
    }

    public void setEnables(GraphEnables graphEnables) {
        displayGrid = graphEnables.isDisplayGrid();
        displayStems = graphEnables.isDisplayStems();
        displayLabels = graphEnables.isDisplayLabels();
        displayLegend = graphEnables.isDisplayLegend();
    }

}
