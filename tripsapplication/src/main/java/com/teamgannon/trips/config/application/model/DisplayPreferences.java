package com.teamgannon.trips.config.application.model;

import javafx.scene.paint.Color;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DisplayPreferences implements Serializable {

    @Serial
    private static final long serialVersionUID = -566336845614891210L;

    private boolean showGrid = true;

    private boolean showScope = true;

    private int gridScale = 5;

    private transient Color gridColor = Color.BLUE;

    private transient Color stemColor = Color.BLUE;

}
