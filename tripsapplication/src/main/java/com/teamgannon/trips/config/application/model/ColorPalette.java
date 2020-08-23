package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.jpa.model.GraphPersistValues;
import javafx.scene.paint.Color;
import lombok.Data;

@Data
public class ColorPalette {

    private Color labelColor;

    private Color gridColor;

    private Color extensionColor;

    private Color legendColor;

    public void assignColors(GraphPersistValues graphPersistValues) {
        labelColor = Color.valueOf(graphPersistValues.getLabelColor());
        gridColor = Color.valueOf(graphPersistValues.getGridColor());
        extensionColor = Color.valueOf(graphPersistValues.getExtensionColor());
        legendColor = Color.valueOf(graphPersistValues.getLegendColor());
    }

    public void setLabelColor(String color) {
        labelColor = Color.valueOf(color);
    }


    public void setGridColor(String color) {
        gridColor = Color.valueOf(color);
    }

    public void setExtensionColor(String color) {
        extensionColor = Color.valueOf(color);
    }

    public void setLegendColor(String color) {
        legendColor = Color.valueOf(color);
    }

    public static ColorPalette defaultColors() {
        ColorPalette palette = new ColorPalette();
        palette.assignColors(GraphPersistValues.defaults());
        return palette;
    }

    public GraphPersistValues getGraphColor() {
        GraphPersistValues graphPersistValues = new GraphPersistValues();
        graphPersistValues.setLabelColor(labelColor.toString());
        graphPersistValues.setGridColor(gridColor.toString());
        graphPersistValues.setExtensionColor(extensionColor.toString());
        graphPersistValues.setLegendColor(legendColor.toString());
        return graphPersistValues;
    }

}
