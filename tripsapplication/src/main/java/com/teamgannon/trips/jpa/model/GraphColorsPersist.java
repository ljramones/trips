package com.teamgannon.trips.jpa.model;

import com.teamgannon.trips.config.application.model.ColorPalette;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
public class GraphColorsPersist implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    /**
     * label color on graph
     */
    private String labelColor = Color.BEIGE.toString();

    /**
     * the font for labels
     */
    private String labelFont = "Arial:8";

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

    /**
     * the extension stem width
     */
    private double stemLineWidth = 0.5;

    /**
     * the width of the grid lines
     */
    private double gridLineWidth = 0.5;


    public static @NotNull GraphColorsPersist defaults() {
        GraphColorsPersist colorsPersist = new GraphColorsPersist();
        colorsPersist.setId(UUID.randomUUID().toString());
        return colorsPersist;
    }

    public @NotNull ColorPalette getColorPalette() {
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
        stemLineWidth = 0.5;
        gridLineWidth = 0.5;
        labelFont = "Arial:8";
    }

    public void setGraphColors(@NotNull ColorPalette graphColors) {
        labelColor = graphColors.getLabelColor().toString();
        gridColor = graphColors.getGridColor().toString();
        extensionColor = graphColors.getExtensionColor().toString();
        legendColor = graphColors.getLegendColor().toString();
        stemLineWidth = graphColors.getStemLineWidth();
        gridLineWidth = graphColors.getGridLineWidth();
        labelFont = graphColors.getLabelFont().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        GraphColorsPersist that = (GraphColorsPersist) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
