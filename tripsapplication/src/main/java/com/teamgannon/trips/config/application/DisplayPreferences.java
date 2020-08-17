package com.teamgannon.trips.config.application;

import lombok.Data;

import java.awt.*;
import java.io.Serializable;

@Data
public class DisplayPreferences implements Serializable {

    private static final long serialVersionUID = -566336845614891210L;

    private boolean showGrid = true;

    private boolean showScope = true;

    private int gridScale = 5;

    private Color gridColor = Color.BLUE;

    private Color stemColor = Color.BLUE;

}
