package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.jpa.model.GraphColorsPersist;
import javafx.scene.paint.Color;
import lombok.Data;

@Data
public class ColorPalette {

    private Color labelColor;

    private Color gridColor;

    private Color extensionColor;

    private Color legendColor;

    public void assignColors(GraphColorsPersist graphColorsPersist) {
        labelColor = Color.valueOf(graphColorsPersist.getLabelColor());
        gridColor = Color.valueOf(graphColorsPersist.getGridColor());
        extensionColor = Color.valueOf(graphColorsPersist.getExtensionColor());
        legendColor = Color.valueOf(graphColorsPersist.getLegendColor());
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
        palette.assignColors(GraphColorsPersist.defaults());
        return palette;
    }

}
