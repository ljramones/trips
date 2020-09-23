package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.jpa.model.GraphColorsPersist;
import javafx.scene.paint.Color;
import lombok.Data;

@Data
public class ColorPalette {

    private String id;

    private Color labelColor;

    private Color gridColor;

    private Color extensionColor;

    private Color legendColor;

    private double stemLineWidth = 0.5;

    private double gridLineWidth = 0.5;

    public static ColorPalette defaultColors() {
        ColorPalette palette = new ColorPalette();
        palette.assignColors(GraphColorsPersist.defaults());
        return palette;
    }

    public void assignColors(GraphColorsPersist graphColorsPersist) {
        id = graphColorsPersist.getId();
        labelColor = Color.valueOf(graphColorsPersist.getLabelColor());
        gridColor = Color.valueOf(graphColorsPersist.getGridColor());
        extensionColor = Color.valueOf(graphColorsPersist.getExtensionColor());
        legendColor = Color.valueOf(graphColorsPersist.getLegendColor());
        stemLineWidth = graphColorsPersist.getStemLineWidth();
        gridLineWidth = graphColorsPersist.getGridLineWidth();
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

    public void setDefaults() {
        labelColor = Color.BEIGE;
        gridColor = Color.MEDIUMBLUE;
        extensionColor = Color.DARKSLATEBLUE;
        legendColor = Color.BEIGE;
        stemLineWidth = 0.5;
        gridLineWidth = 0.5;
    }
}
