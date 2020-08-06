package com.teamgannon.trips.jpa.model;

import com.teamgannon.trips.algorithms.graphtraversal.Graph;
import javafx.scene.paint.Color;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
public class GraphColor implements Serializable {

    @Id
    private String id;

    private String labelColor;

    private String gridColor;

    private String extensionColor;

    private String legendColor;

    public void initColors() {
        id = UUID.randomUUID().toString();
        labelColor = Color.BEIGE.toString();
        gridColor = Color.MEDIUMBLUE.toString();
        extensionColor = Color.DARKSLATEBLUE.toString();
        legendColor = Color.BEIGE.toString();
    }

    public static GraphColor defaultColors() {
        GraphColor graphColor = new GraphColor();
        graphColor.initColors();
        return graphColor;
    }

}
