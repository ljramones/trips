package com.teamgannon.trips.jpa.model;

import com.teamgannon.trips.config.application.model.ColorPalette;
import javafx.scene.paint.Color;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Data
@Entity
public class GraphColorsPersist {

    @Id
    private String id;

    /**
     * label color on graph
     */
    private String labelColor = Color.BEIGE.toString();

    /**
     * the grid color on the graph
     */
    private String gridColor = Color.MEDIUMBLUE.toString();

    /**
     * the stem color on the graph
     */
    private String extensionColor = Color.DARKSLATEBLUE.toString();

    /**
     * the legend color on the graph
     */
    private String legendColor = Color.BEIGE.toString();

    public static GraphColorsPersist defaults() {
        GraphColorsPersist colorsPersist = new GraphColorsPersist();
        colorsPersist.setId(UUID.randomUUID().toString());
        return colorsPersist;
    }

    public ColorPalette getColorPalette() {
        ColorPalette palette = new ColorPalette();
        palette.assignColors(this);
        return palette;
    }

    /**
     * initialize the colors with a default
     */
    public void init() {
        id = UUID.randomUUID().toString();
        setToDefault();
    }

    public void setToDefault() {
        labelColor = Color.BEIGE.toString();
        gridColor = Color.MEDIUMBLUE.toString();
        extensionColor = Color.DARKSLATEBLUE.toString();
        legendColor = Color.BEIGE.toString();
    }

    public void setGraphColors(ColorPalette graphColors) {
        labelColor = graphColors.getLabelColor().toString();
        gridColor = graphColors.getGridColor().toString();
        extensionColor = graphColors.getExtensionColor().toString();
        legendColor = graphColors.getLegendColor().toString();
    }
}
