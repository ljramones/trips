package com.teamgannon.trips.config.application.model;

import com.teamgannon.trips.planetarymodelling.planetgen.math.Color;
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

    private Color gridColor = Color.BLUE;

    private Color stemColor = Color.BLUE;

}
