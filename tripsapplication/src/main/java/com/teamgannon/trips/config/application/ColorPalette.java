package com.teamgannon.trips.config.application;

import com.teamgannon.trips.jpa.model.GraphColor;
import javafx.scene.paint.Color;
import lombok.Data;

@Data
public class ColorPalette {

    private Color labelColor;

    private Color gridColor;

    private Color extensionColor;

    private Color legendColor;

    public void assignColors(GraphColor graphColor) {
        labelColor = Color.valueOf(graphColor.getLabelColor());
        gridColor = Color.valueOf(graphColor.getGridColor());
        extensionColor = Color.valueOf(graphColor.getExtensionColor());
        legendColor = Color.valueOf(graphColor.getLegendColor());
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
        palette.assignColors(GraphColor.defaultColors());
        return palette;
    }

    public GraphColor getGraphColor() {
        GraphColor graphColor = new GraphColor();
        graphColor.setLabelColor(labelColor.toString());
        graphColor.setGridColor(gridColor.toString());
        graphColor.setExtensionColor(extensionColor.toString());
        graphColor.setLegendColor(legendColor.toString());
        return graphColor;
    }

}
