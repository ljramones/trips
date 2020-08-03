package com.teamgannon.trips.config.application;

import lombok.Data;

import java.awt.*;

@Data
public class DisplayPreferences {

    private boolean showGrid = true;

    private boolean showScope = true;

    private int gridScale = 5;

    private Color gridColor = Color.BLUE;

    private Color stemColor = Color.BLUE;

}
